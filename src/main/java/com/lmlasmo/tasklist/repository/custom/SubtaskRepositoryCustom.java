package com.lmlasmo.tasklist.repository.custom;

import java.math.BigDecimal;
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
		
	public Flux<SubtaskSummary.PositionSummary> findPositionSummaryByTaskIdOrderByASC(int taskId);
		
	public Mono<SubtaskSummary.PositionSummary> findPositionSummaryById(int subtaskId);
		
	public Flux<PositionSummary> findPositionSummaryByRelatedSubtaskId(int subtaskId);
	
	public Mono<PositionSummary> findFirstPositionSummaryByTaskIdOrderByASC(int taskId);
	
	public Mono<PositionSummary> findLastPositionSummaryByTaskIdOrderByASC(int taskId);
	
	public Mono<PositionSummary> findFirstPositionSummaryByTaskIdAndPositionGreaterThanOrderByASC(int taskId, BigDecimal position);
	
	public Mono<PositionSummary> findFirstPositionSummaryByTaskIdAndPositionLessThanOrderByDESC(int taskId, BigDecimal position);
	
	public Mono<Void> updatePriority(BasicSummary basic, BigDecimal position);
	
	public Mono<SubtaskSummary.StatusSummary> findStatusSummaryById(int subtaskId);
	
	public Flux<SubtaskSummary.StatusSummary> findStatusSummaryByIds(Collection<Integer> subtaskIds);
	
	public Mono<Void> updateStatus(BasicSummary basic, TaskStatusType status);
	
	public Mono<Void> updateStatus(Collection<? extends BasicSummary> basics, TaskStatusType status);
	
	public Mono<Long> sumVersionByids(Collection<Integer> ids);
	
	public Mono<Long> sumVersionByTask(int taskId);
	
}
