package com.lmlasmo.tasklist.service;

import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.CountDTO;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.exception.ResourceNotDeletableException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.mapper.TaskMapper;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.service.applier.UpdateTaskApplier;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class TaskService {
	
	private TaskRepository repository;
	private TaskMapper mapper;

	public Mono<TaskDTO> save(CreateTaskDTO create, int userId) {
		return Mono.just(mapper.toEntity(create))
				.doOnNext(t -> t.setUserId(userId))
				.flatMap(repository::save)
				.map(mapper::toDTO);
	}
	
	public Mono<Void> delete(int id) {
		return repository.existsById(id)
				.filter(Boolean::booleanValue)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Task not found")))
				.then(repository.deleteById(id))
				.then(repository.existsById(id))
				.filter(e -> !e)
				.switchIfEmpty(Mono.error(new ResourceNotDeletableException("Task not deleted")))
				.then();
	}
	
	public Mono<TaskDTO> update(int taskId, UpdateTaskDTO update) {
		return repository.findById(taskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Task not found for id equals " + taskId)))
				.doOnNext(t -> UpdateTaskApplier.apply(update, t))
				.flatMap(repository::save)
				.map(mapper::toDTO);
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
	
	public Mono<Long> sumVersionByUser(int userId, Pageable pageable, String contains, TaskStatusType status) {
		return repository.sumVersionByUser(userId, pageable, contains, status);
	}
	
	public Flux<TaskDTO> findByUser(int id, Pageable pageable, String contains, TaskStatusType status) {
		return repository.findAllByUserId(id, pageable, contains, status)
				.map(mapper::toDTO);
	}

	public Mono<TaskDTO> findById(int taskId) {
		return repository.findById(taskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Task not found for id equals " + taskId)))
				.map(mapper::toDTO);
	}
	
	public Mono<CountDTO> countByUser(int userId) {
		return repository.countByUserId(userId)
				.map(c -> new CountDTO("task", c));
	}

}
