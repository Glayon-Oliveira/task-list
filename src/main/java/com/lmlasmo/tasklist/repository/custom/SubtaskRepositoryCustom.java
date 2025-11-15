package com.lmlasmo.tasklist.repository.custom;

import java.util.Collection;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubtaskRepositoryCustom extends RepositoryCustom {
	
	public Mono<Boolean> existsByIdAndTaskUserId(int subtaskId, int userId);

	public Mono<Long> countByIdInAndTaskUserId(Collection<Integer> subtaskIds, int userId);
		
	public Flux<SubtaskSummary.PositionSummary> findPositionSummaryByTaskId(int taskId);
		
	public Mono<SubtaskSummary.PositionSummary> findPositionSummaryById(int subtaskId);
		
	public Flux<PositionSummary> findPositionSummaryByRelatedSubtaskId(int subtaskId);
	
	public Mono<Void> updatePriority(BasicSummary basic, int position);
	
	public Mono<SubtaskSummary.StatusSummary> findStatusSummaryById(int subtaskId);
	
	public Flux<SubtaskSummary.StatusSummary> findStatusSummaryByIds(Collection<Integer> subtaskIds);
	
	public Mono<Void> updateStatus(BasicSummary basic, TaskStatusType status);
	
	public Mono<Void> updateStatus(Collection<? extends BasicSummary> basics, TaskStatusType status);
	
	public Mono<Long> sumVersionByids(Collection<Integer> ids);
	
	public Mono<Long> sumVersionByTask(int taskId);
	
}
