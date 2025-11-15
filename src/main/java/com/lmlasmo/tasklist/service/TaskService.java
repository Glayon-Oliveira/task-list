package com.lmlasmo.tasklist.service;

import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.service.applier.UpdateTaskApplier;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class TaskService {
	
	private TaskRepository repository;

	public Mono<TaskDTO> save(CreateTaskDTO create, int userId) {
		return Mono.just(new Task(create, userId))
				.flatMap(repository::save)
				.map(TaskDTO::new);
	}
	
	public Mono<Void> delete(int id) {
		return repository.existsById(id)
				.filter(Boolean::booleanValue)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Task not found")))
				.then(repository.deleteById(id))
				.then(repository.existsById(id))
				.filter(e -> !e)
				.switchIfEmpty(Mono.error(new EntityNotDeleteException("Task not deleted")))
				.then();
	}
	
	public Mono<TaskDTO> update(int taskId, UpdateTaskDTO update) {
		return repository.findById(taskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Task not found for id equals " + taskId)))
				.doOnNext(t -> UpdateTaskApplier.apply(update, t))
				.flatMap(repository::save)
				.map(TaskDTO::new);
	}	
	
	public Mono<Boolean> existsByIdAndVersion(int id, long version) {
		return repository.existsByIdAndVersion(id, version);
	}
	
	public Mono<Long> sumVersionByIds(Collection<Integer> ids) {
		return repository.sumVersionByids(ids);
	}
	
	public Mono<Long> sumVersionByUser(int userId) {
		return repository.sumVersionByUser(userId);
	}
	
	public Flux<TaskDTO> findByUser(int id) {
		return repository.findByUserId(id)
				.map(TaskDTO::new);
	}
	
	public Flux<TaskDTO> findAll(Pageable pageable){
		return repository.findAll()
				.map(TaskDTO::new);
	}

	public Mono<TaskDTO> findById(int taskId) {
		return repository.findById(taskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Task not found for id equals " + taskId)))
				.map(TaskDTO::new);
	}	

}
