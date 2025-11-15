package com.lmlasmo.tasklist.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.repository.custom.TaskRepositoryCustom;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskRepository extends ReactiveCrudRepository<Task, Integer>, TaskRepositoryCustom {

	public Flux<Task> findByUserId(int id);

	public Mono<Boolean> existsByIdAndUserId(int taskId, int userId);

	public Mono<Boolean> existsByIdAndVersion(int id, long version);
	
}
