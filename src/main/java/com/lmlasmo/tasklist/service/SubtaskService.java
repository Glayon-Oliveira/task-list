package com.lmlasmo.tasklist.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.cache.ReactiveCache;
import com.lmlasmo.tasklist.dto.CountDTO;
import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskPositionDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskPositionDTO.MovePositionType;
import com.lmlasmo.tasklist.exception.InvalidDataRequestException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.mapper.SubtaskMapper;
import com.lmlasmo.tasklist.mapper.summary.SubtaskSummaryMapper;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.service.applier.UpdateSubtaskApplier;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class SubtaskService {
	
	private static final String CV_FIND_TASKID_TEMPLATE = "tId:%d;pfh:%d;find";
	private static final String CV_FIND_ID_TEMPLATE = "stId:%s;find";
	private static final String CV_SUM_VERSION_TEMPLATE = "tId:%d;pfh:%d;sum&version";
	private static final String CV_COUNT_TEMPLATE = "tId:%d;count";
	private static final String CV_EXISTS_ID_VERSION_TEMPLATE = "stId:%d;exists&version";

	@NonNull private SubtaskRepository subtaskRepository;
	@NonNull private SubtaskMapper mapper;
	@NonNull private SubtaskSummaryMapper summaryMapper;
	@NonNull private ReactiveCache cache;
	private final BigDecimal positionStep = BigDecimal.valueOf(1024);
		
	public Mono<SubtaskDTO> save(CreateSubtaskDTO create) {
		return Mono.just(mapper.toEntity(create))
				.flatMap(s -> {
					return subtaskRepository.findPositionSummaryByTaskIdOrderByASC(create.getTaskId())
							.map(PositionSummary::getPosition)
							.reduce(BigDecimal::max)
							.doOnNext(m -> s.setPosition(m.add(positionStep)))
							.thenReturn(s);
				})
				.flatMap(subtaskRepository::save)
				.map(mapper::toDTO);
	}
	
	public Mono<Void> delete(List<Integer> subtaskIds) {
		return Flux.fromIterable(subtaskIds)
				.collectList()
				.flatMap(subtaskRepository::deleteAllById)
				.then();
	}
	
	public Mono<SubtaskDTO> update(int subtaskId, UpdateSubtaskDTO update) {
		return subtaskRepository.findById(subtaskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id equals " + subtaskId)))
				.doOnNext(s -> UpdateSubtaskApplier.apply(update, s))
				.flatMap(subtaskRepository::save)
				.map(mapper::toDTO);
	}
	
	public Mono<Void> updatePosition(int subtaskId, UpdateSubtaskPositionDTO update) {
		if(subtaskId == update.getAnchorSubtaskId()) return Mono.error(new InvalidDataRequestException("Subtask id "+ subtaskId +" not can equals anchor subtask id " + update.getAnchorSubtaskId()));
		
		return subtaskRepository.findPositionSummaryById(subtaskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id " + subtaskId)))
				.flatMap(s -> {
					return subtaskRepository.findPositionSummaryById(update.getAnchorSubtaskId())
							.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id " + update.getAnchorSubtaskId())))
							.flatMap(a -> updatePositionForMidOrLimit(s, a, update.getMoveType()));
				});
	}
	
	private Mono<Void> updatePositionForMidOrLimit(PositionSummary subtask, PositionSummary anchorSubtask, MovePositionType moveType) {		
		boolean isBefore = MovePositionType.BEFORE.equals(moveType);
		BigDecimal nStep = positionStep.abs();
		
		Mono<PositionSummary> lAnc = isBefore ? subtaskRepository.findFirstPositionSummaryByTaskIdAndPositionLessThanOrderByDESC(anchorSubtask.getTaskId(), anchorSubtask.getPosition()).cache()
											  : subtaskRepository.findFirstPositionSummaryByTaskIdAndPositionGreaterThanOrderByASC(anchorSubtask.getTaskId(), anchorSubtask.getPosition()).cache();
		
		return lAnc
				.hasElement()
				.flatMap(has -> {
					if(has) {
						return lAnc.flatMap(lA -> {
							BigDecimal mid = anchorSubtask.getPosition()
									.add(lA.getPosition())
									.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
							return subtaskRepository.updatePriority(subtask, mid);
						});
					}
					
					BigDecimal extreme = isBefore ? anchorSubtask.getPosition().divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP)
												  : anchorSubtask.getPosition().add(nStep);
					
					return subtaskRepository.updatePriority(subtask, extreme);
				})
				.onErrorResume(DataIntegrityViolationException.class, e -> normalizePositions(subtask, anchorSubtask, moveType, nStep)
						.then(Mono.defer(() -> {
							UpdateSubtaskPositionDTO update = new UpdateSubtaskPositionDTO(moveType, anchorSubtask.getId());							
							return updatePosition(subtask.getId(), update);
						})))
				.as(subtaskRepository.getOperator()::transactional);
	}
	
	private Mono<Void> normalizePositions(PositionSummary subtask, PositionSummary anchorSubtask, MovePositionType moveType, BigDecimal step) {
		return subtaskRepository.findPositionSummaryByTaskIdOrderByASC(subtask.getTaskId())
				.index()
				.concatMap(ts -> {
					BigDecimal position = BigDecimal.valueOf(-3).multiply(BigDecimal.valueOf(ts.getT1()+1));
					
					return subtaskRepository.updatePriority(ts.getT2(), position)
							.thenReturn(summaryMapper.toPositionSummary(
									ts.getT2().getId(),
									ts.getT2().getVersion()+1,
									0,
									position));
				})
				.index()
				.concatMap(ts -> {
					BigDecimal position = step.multiply(BigDecimal.valueOf(ts.getT1()+1));
					return subtaskRepository.updatePriority(ts.getT2(), position);
				})
				.as(subtaskRepository.getOperator()::transactional)
				.then();
	}
	
	public Mono<Boolean> existsByIdAndVersion(int id, long version) {
		return cache.get(CV_EXISTS_ID_VERSION_TEMPLATE.formatted(id, version), Boolean.class)
				.switchIfEmpty(subtaskRepository.existsByIdAndVersion(id, version)
							.doOnNext(e -> cache.put(CV_EXISTS_ID_VERSION_TEMPLATE.formatted(id, version), e))
						);
	}
		
	public Mono<Long> sumVersionByIds(Collection<Integer> ids) {
		return subtaskRepository.sumVersionByids(ids);
	}
	
	public Mono<Long> sumVersionByTask(int taskId) {
		return cache.get(CV_SUM_VERSION_TEMPLATE.formatted(taskId, 0), Long.class)
				.switchIfEmpty(subtaskRepository.sumVersionByTask(taskId)
							.doOnNext(s -> cache.put(CV_SUM_VERSION_TEMPLATE.formatted(taskId), s))
						);
	}
		
	public Mono<Long> sumVersionByTask(int taskId, Pageable pageable, String contains, TaskStatusType status) {
		int pfh = Objects.hash(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				pageable.getSort(),
				contains,
				status
				);
		
		return cache.get(CV_SUM_VERSION_TEMPLATE.formatted(taskId, pfh), Long.class)
				.switchIfEmpty(subtaskRepository.sumVersionByTask(taskId, pageable, contains, status)
							.doOnNext(s -> cache.put(CV_SUM_VERSION_TEMPLATE.formatted(taskId), s))
						);
	}
	
	public Flux<SubtaskDTO> findByTask(int taskId, Pageable pageable, String contains, TaskStatusType status){
		return findByTask(taskId, pageable, contains, status, new String[0]);
	}
	
	@SuppressWarnings("unchecked")
	public Flux<SubtaskDTO> findByTask(int taskId, Pageable pageable, String contains, TaskStatusType status, String... fields){
		int pfh = Objects.hash(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				pageable.getSort(),
				contains,
				status,
				fields
				);
		
		return cache.get(CV_FIND_TASKID_TEMPLATE.formatted(taskId, pfh), Collection.class)
				.switchIfEmpty(subtaskRepository.findAllByTaskId(taskId, pageable, contains, status, fields)
							.map(mapper::toDTO)
							.collectList()
							.doOnNext(dto -> cache.put(CV_FIND_TASKID_TEMPLATE.formatted(taskId, pfh), dto))
						)
				.flatMapMany(Flux::fromIterable);
	}
	
	public Mono<SubtaskDTO> findById(int subtaskId) {
		return cache.get(CV_FIND_ID_TEMPLATE.formatted(subtaskId), SubtaskDTO.class)
				.switchIfEmpty(subtaskRepository.findById(subtaskId)
							.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id equals " + subtaskId)))
							.map(mapper::toDTO)
							.doOnNext(dto -> cache.put(CV_FIND_ID_TEMPLATE.formatted(subtaskId), dto))
						);
	}
		
	public Mono<CountDTO> countByTask(int taskId) {
		return cache.get(CV_COUNT_TEMPLATE.formatted(taskId), Long.class)
				.switchIfEmpty(subtaskRepository.countByTaskId(taskId)
							.doOnNext(c -> cache.put(CV_COUNT_TEMPLATE.formatted(taskId), c))
						)
				.map(c -> new CountDTO("subtask", c));
	}
	
}
