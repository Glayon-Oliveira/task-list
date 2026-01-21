package com.lmlasmo.tasklist.repository.custom;

import java.util.Collection;

import org.springframework.data.domain.Pageable;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskRepositoryCustom extends RepositoryCustom {
	
	public Mono<Long> countByUserId(int userId);
	
	public Flux<Task> findAllByUserId(int userId, Pageable pageable, String contains, TaskStatusType status);
	
	public Mono<StatusSummary> findStatusSummaryById(int taskId);

	public Mono<Void> updateStatus(BasicSummary basic, TaskStatusType status);
	
	public Mono<Long> sumVersionByids(Collection<Integer> ids);
	
	public Mono<Long> sumVersionByUser(int userId);
	
	public Mono<Long> sumVersionByUser(int userId, Pageable pageable, String contains, TaskStatusType status);
	
}
