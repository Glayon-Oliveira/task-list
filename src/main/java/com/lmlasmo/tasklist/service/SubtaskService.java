package com.lmlasmo.tasklist.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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
import com.lmlasmo.tasklist.repository.summary.Field;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;
import com.lmlasmo.tasklist.service.applier.UpdateSubtaskApplier;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class SubtaskService {
	
	private static final String CV_FIND_TASKID_TEMPLATE = "tId:%d;pfh:%d;find";
	private static final String CV_FIND_ID_TEMPLATE = "stId:%s;pfh:%d;find";
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
					Sort positionAsc = Sort.by(Order.asc("position"));
					
					return subtaskRepository.findSummariesByTaskIdAndSort(create.getTaskId(), positionAsc, Set.of("position"))
							.map(ss -> ss.getPosition().get())
							.reduce(BigDecimal::max)
							.map(positionStep::add)
							.switchIfEmpty(Mono.just(BigDecimal.ZERO))
							.doOnNext(s::setPosition)
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
		
		return subtaskRepository.findSummaryById(subtaskId, Set.of("position"))
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id " + subtaskId)))
				.flatMap(s -> {
					return subtaskRepository.findSummaryById(update.getAnchorSubtaskId(), Set.of("position"))
							.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id " + update.getAnchorSubtaskId())))
							.flatMap(a -> updatePositionForMidOrLimit(s, a, update.getMoveType()));
				});
	}
	
	private Mono<Void> updatePositionForMidOrLimit(SubtaskSummary subtask, SubtaskSummary anchorSubtask, MovePositionType moveType) {		
		boolean isBefore = MovePositionType.BEFORE.equals(moveType);
		BigDecimal nStep = positionStep.abs();
		
		Pageable firstLessDesc = PageRequest.of(0, 1, Sort.by(Order.desc("position")));
		Pageable firstGreaterASC = PageRequest.of(0, 1, Sort.by(Order.desc("position")));
		
		Mono<SubtaskSummary> lAnc = isBefore 
				? subtaskRepository.findSummaryByTaskIdAndPositionLessThan(
						anchorSubtask.getTaskId().get(), 
						anchorSubtask.getPosition().get(), 
						firstLessDesc, Set.of("position")
						).next().cache()
				: subtaskRepository.findSummaryByTaskIdAndPositionLessThan(
						anchorSubtask.getTaskId().get(),
						anchorSubtask.getPosition().get(),
						firstGreaterASC,
						Set.of("position")
						).next().cache();
		
		return lAnc
				.hasElement()
				.flatMap(has -> {
					if(has) {
						return lAnc.flatMap(lA -> {
							BigDecimal mid = anchorSubtask.getPosition().get()
									.add(lA.getPosition().get())
									.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
							return subtaskRepository.updatePriority(subtask, mid);
						});
					}
					
					BigDecimal extreme = isBefore 
							? anchorSubtask.getPosition().get()
									.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP)
							: anchorSubtask.getPosition().get()
									.add(nStep);
					
					return subtaskRepository.updatePriority(subtask, extreme);
				})
				.onErrorResume(DataIntegrityViolationException.class, e -> normalizePositions(subtask, anchorSubtask, moveType, nStep)
						.then(Mono.defer(() -> {
							UpdateSubtaskPositionDTO update = new UpdateSubtaskPositionDTO(moveType, anchorSubtask.getId().get());
							return updatePosition(subtask.getId().get(), update);
						})))
				.as(subtaskRepository.getOperator()::transactional);
	}
	
	private Mono<Void> normalizePositions(SubtaskSummary subtask, SubtaskSummary anchorSubtask, MovePositionType moveType, BigDecimal step) {
		Sort positionAsc = Sort.by(Order.asc("position"));
		
		return subtaskRepository.findSummariesByTaskIdAndSort(subtask.getTaskId().get(), positionAsc, Set.of("position"))
				.index()
				.concatMap(ts -> {
					SubtaskSummary summary = ts.getT2();
					BigDecimal position = BigDecimal.valueOf(-3).multiply(BigDecimal.valueOf(ts.getT1()+1));
					
					Long previusVersion = summary.getVersion().get()+1;
					
					SubtaskSummary previusSummary = new SubtaskSummary(
							summary.getId(), summary.getName(), summary.getSummary(), summary.getStatus(),
							Field.of(position), summary.getDurationMinutes(), Field.of(previusVersion),
							summary.getCreatedAt(), summary.getUpdatedAt(), summary.getTaskId()
							);
					
					return subtaskRepository.updatePriority(ts.getT2(), position)
							.thenReturn(previusSummary);
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
		ParameterizedTypeReference<Boolean> booleanType = new ParameterizedTypeReference<Boolean>() {};
		
		return cache.get(CV_EXISTS_ID_VERSION_TEMPLATE.formatted(id, version), booleanType)
				.switchIfEmpty(subtaskRepository.existsByIdAndVersion(id, version)
							.doOnNext(e -> cache.asyncPut(CV_EXISTS_ID_VERSION_TEMPLATE.formatted(id, version), e))
						);
	}
		
	public Mono<Long> sumVersionByIds(Collection<Integer> ids) {
		return subtaskRepository.sumVersionByids(ids);
	}
	
	public Mono<Long> sumVersionByTask(int taskId) {
		ParameterizedTypeReference<Long> longType = new ParameterizedTypeReference<Long>() {};
		
		return cache.get(CV_SUM_VERSION_TEMPLATE.formatted(taskId, 0), longType)
				.switchIfEmpty(subtaskRepository.sumVersionByTask(taskId)
							.doOnNext(s -> cache.asyncPut(CV_SUM_VERSION_TEMPLATE.formatted(taskId, 0), s))
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
		
		ParameterizedTypeReference<Long> longType = new ParameterizedTypeReference<Long>() {};
		
		return cache.get(CV_SUM_VERSION_TEMPLATE.formatted(taskId, pfh), longType)
				.switchIfEmpty(subtaskRepository.sumVersionByTask(taskId, pageable, contains, status)
							.doOnNext(s -> cache.asyncPut(CV_SUM_VERSION_TEMPLATE.formatted(taskId, pfh), s))
						);
	}
	
	public Flux<Map<String, Object>> findByTask(int taskId, Pageable pageable, String contains, TaskStatusType status){
		return findByTask(taskId, pageable, contains, status, Set.of());
	}
	
	public Flux<Map<String, Object>> findByTask(int taskId, Pageable pageable, String contains, TaskStatusType status, Set<String> fields){
		int pfh = Objects.hash(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				pageable.getSort(),
				contains,
				status,
				fields
				);
		
		ParameterizedTypeReference<Collection<Map<String, Object>>> dtoCollectionType = new ParameterizedTypeReference<Collection<Map<String, Object>>>() {};
		
		return cache.get(CV_FIND_TASKID_TEMPLATE.formatted(taskId, pfh), dtoCollectionType)
				.switchIfEmpty(subtaskRepository.findSummariesByTaskId(taskId, pageable, contains, status, fields)
							.map(SubtaskSummary::toMap)
							.collectList()
							.doOnNext(dto -> cache.asyncPut(CV_FIND_TASKID_TEMPLATE.formatted(taskId, pfh), dto))
						)
				.flatMapMany(Flux::fromIterable);
	}
	
	public Mono<SubtaskDTO> findById(int subtaskId) {
		ParameterizedTypeReference<SubtaskDTO> dtoType = new ParameterizedTypeReference<SubtaskDTO>() {};
		
		return cache.get(CV_FIND_ID_TEMPLATE.formatted(subtaskId, 0), dtoType)
				.switchIfEmpty(subtaskRepository.findById(subtaskId)
							.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id equals " + subtaskId)))
							.map(mapper::toDTO)
							.doOnNext(dto -> cache.asyncPut(CV_FIND_ID_TEMPLATE.formatted(subtaskId, 0), dto))
						);
	}
	
	public Mono<Map<String, Object>> findById(int subtaskId, Set<String> fields) {
		long pfh = Objects.hash(subtaskId, fields);
		
		ParameterizedTypeReference<Map<String, Object>> dtoType = new ParameterizedTypeReference<Map<String, Object>>() {};
		
		return cache.get(CV_FIND_ID_TEMPLATE.formatted(subtaskId, pfh), dtoType)
				.switchIfEmpty(subtaskRepository.findSummaryById(subtaskId, fields)
							.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id equals " + subtaskId)))
							.map(SubtaskSummary::toMap)
							.doOnNext(dto -> cache.asyncPut(CV_FIND_ID_TEMPLATE.formatted(subtaskId, pfh), dto))
						);
	}
		
	public Mono<CountDTO> countByTask(int taskId) {
		ParameterizedTypeReference<Long> longType = new ParameterizedTypeReference<Long>() {};
		
		return cache.get(CV_COUNT_TEMPLATE.formatted(taskId), longType)
				.switchIfEmpty(subtaskRepository.countByTaskId(taskId)
							.doOnNext(c -> cache.asyncPut(CV_COUNT_TEMPLATE.formatted(taskId), c))
						)
				.map(c -> new CountDTO("subtask", c));
	}
	
}
