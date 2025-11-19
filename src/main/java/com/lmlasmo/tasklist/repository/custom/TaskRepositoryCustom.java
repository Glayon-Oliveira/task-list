package com.lmlasmo.tasklist.repository.custom;

import java.util.Collection;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

import reactor.core.publisher.Mono;

public interface TaskRepositoryCustom extends RepositoryCustom {
	
	public Mono<StatusSummary> findStatusSummaryById(int taskId);

	public Mono<Void> updateStatus(BasicSummary basic, TaskStatusType status);
	
	public Mono<Long> sumVersionByids(Collection<Integer> ids);
	
	public Mono<Long> sumVersionByUser(int userId);
	
}
