package com.lmlasmo.tasklist.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.custom.SubtaskRepositoryCustom;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubtaskRepository extends ReactiveCrudRepository<Subtask, Integer>, SubtaskRepositoryCustom {

	public Flux<Subtask> findByTaskId(int taskId);
	
	public Mono<Boolean> existsByTaskIdAndStatus(int taskId, TaskStatusType status);
	
	public Mono<Boolean> existsByTaskIdAndStatusNot(int taskId, TaskStatusType status);
	
	public Mono<Boolean> existsByIdAndVersion(int id, long version);
		
	public Mono<Long> countByTaskId(int taskId);
	
}
