package com.lmlasmo.tasklist.repository.custom;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubtaskRepositoryCustom extends RepositoryCustom {
	
	public Mono<Long> countByTaskId(int userId);
	
	public Mono<Boolean> existsByIdAndTaskUserId(int subtaskId, int userId);

	public Mono<Long> countByIdInAndTaskUserId(Collection<Integer> subtaskIds, int userId);
	
	public Mono<SubtaskSummary> findSummaryById(int subtaskId, Set<String> includedFields);
	
	public Flux<SubtaskSummary> findSummaryByIds(Collection<Integer> subtaskIds, Set<String> includedFields);
	
	public Flux<SubtaskSummary> findSummariesByTaskId(int taskId, Pageable pageable, String contains, TaskStatusType status, Set<String> includedFields);
	
	public Flux<SubtaskSummary> findSummariesByTaskIdAndSort(int taskId, Sort sort, Set<String> includedFields);
	
	public Flux<SubtaskSummary> findSummaryByRelatedSubtaskId(int subtaskId, Set<String> includedFields);
	
	public Flux<SubtaskSummary> findSummaryByTaskIdAndPositionGreaterThan(int taskId, BigDecimal position, Pageable pageable, Set<String> includedFields);
	
	public Flux<SubtaskSummary> findSummaryByTaskIdAndPositionLessThan(int taskId, BigDecimal position, Pageable pageable, Set<String> includedFields);
	
	public Mono<Void> updatePriority(BasicSummary<Integer> basic, BigDecimal position);	
	
	public Mono<Void> updateStatus(BasicSummary<Integer> basic, TaskStatusType status);
	
	public Mono<Void> updateStatus(Collection<? extends BasicSummary<Integer>> basics, TaskStatusType status);
	
	public Mono<Void> updateStatusByTaskId(int taskId, TaskStatusType status);
	
	public Mono<Long> sumVersionByids(Collection<Integer> ids);
	
	public Mono<Long> sumVersionByTask(int taskId);
	
	public Mono<Long> sumVersionByTask(int taskId, Pageable pageable, String contains, TaskStatusType status);
	
}
