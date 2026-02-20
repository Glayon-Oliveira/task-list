package com.lmlasmo.tasklist.repository.custom;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskRepositoryCustom extends RepositoryCustom {
	
	public Mono<Long> countByUserId(int userId);
	
	public Mono<TaskSummary> findSummaryById(int id, Set<String> includedFields);
	
	public Flux<TaskSummary> findSummariesByUserId(int userId, Pageable pageable, String contains, TaskStatusType status, Set<String> includedFields);

	public Mono<Void> updateStatus(BasicSummary<Integer> basic, TaskStatusType status);
	
	public Mono<Long> sumVersionByids(Collection<Integer> ids);
	
	public Mono<Long> sumVersionByUser(int userId);
	
	public Mono<Long> sumVersionByUser(int userId, Pageable pageable, String contains, TaskStatusType status);
	
}
