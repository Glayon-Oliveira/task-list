package com.lmlasmo.tasklist.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.exception.ResourceAlreadyExistsException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.service.applier.UpdateSubtaskApplier;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class SubtaskService {

	private SubtaskRepository subtaskRepository;
		
	public Mono<SubtaskDTO> save(CreateSubtaskDTO create) {
		return Mono.just(new Subtask(create))
				.flatMap(s -> {
					return subtaskRepository.findPositionSummaryByTaskId(create.getTaskId())
							.map(PositionSummary::getPosition)
							.reduce(Integer::max)
							.doOnNext(s::setPosition)
							.thenReturn(s);
				})
				.flatMap(subtaskRepository::save)
				.map(SubtaskDTO::new);
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
				.map(SubtaskDTO::new);
	}
		
	public Mono<Void> updatePosition(int subtaskId, int position) {
		return subtaskRepository.findPositionSummaryById(subtaskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id " + subtaskId)))
				.flatMap(s -> {
					if(s.getPosition() == position) {
						return Mono.error(new ResourceAlreadyExistsException("New position of subtask is equals at exists position"));
					}
					
					return subtaskRepository.updatePriority(s, 0)
							.thenReturn(new PositionSummary(s.getId(), s.getVersion()+1, s.getPosition()));
				})
				.flatMap(s -> {
					return subtaskRepository.findPositionSummaryByRelatedSubtaskId(subtaskId)
							.collectList()
							.map(ss -> normalizePositions(ss, s, position))
							.flatMapMany(Flux::fromIterable)
							.concatMap(ups -> subtaskRepository.updatePriority(ups, ups.getPosition()))
							.then();
				}).as(m -> subtaskRepository.getOperator().transactional(m));
	}

	private List<PositionSummary> normalizePositions(List<PositionSummary> siblingSubtasks, PositionSummary subtaskToMove, int targetPosition){
		List<PositionSummary> resultList = new ArrayList<>();	
		int maxPosition = siblingSubtasks.size() + 1;
		final int finalPosition = Math.max(1, Math.min(targetPosition, maxPosition));		
		
		boolean isAscRasult = (finalPosition >= subtaskToMove.getPosition());
				
		subtaskToMove = new PositionSummary(subtaskToMove.getId(), subtaskToMove.getVersion(), finalPosition);
		
		siblingSubtasks.sort(Comparator.comparingInt(PositionSummary::getPosition));
		Iterator<PositionSummary> siblingIt = siblingSubtasks.iterator();
		
		for(int pos = 1; pos <= maxPosition; pos++) {
			
			if(finalPosition != pos) {								
				PositionSummary subtask = siblingIt.next();
				siblingIt.remove();
				
				if(subtask.getPosition() != pos) resultList.add(new PositionSummary(subtask.getId(), subtask.getVersion(), pos));
			}
		}
		
		Comparator<PositionSummary> comparator = Comparator.comparingInt(PositionSummary::getPosition);
		
		if(!isAscRasult) comparator = comparator.reversed();
		
		resultList.sort(comparator);
		resultList.add(subtaskToMove);
		
		return Collections.unmodifiableList(resultList);
	}
	
	public Mono<Boolean> existsByIdAndVersion(int id, long version) {
		return subtaskRepository.existsByIdAndVersion(id, version);
	}
	
	public Mono<Long> sumVersionByIds(Collection<Integer> ids) {
		return subtaskRepository.sumVersionByids(ids);
	}
	
	public Mono<Long> sumVersionByTask(int taskId) {
		return subtaskRepository.sumVersionByTask(taskId);
	}
	
	public Flux<SubtaskDTO> findByTask(int taskId){		
		return subtaskRepository.findByTaskId(taskId)
				.map(SubtaskDTO::new);
	}

	public Mono<SubtaskDTO> findById(int subtaskId) {
		return subtaskRepository.findById(subtaskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtask not found for id equals " + subtaskId)))
				.map(SubtaskDTO::new);
	}
	
}
