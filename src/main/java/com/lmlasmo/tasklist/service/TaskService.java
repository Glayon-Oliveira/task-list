package com.lmlasmo.tasklist.service;

import java.util.Collection;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.cache.ReactiveCache;
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
		
	public static final String CV_FIND_USERID_TEMPLATE = "uId:%d;pfh:%d;find";
	public static final String CV_FIND_ID_TEMPLATE = "tId:%d;find";
	public static final String CV_SUM_VERSION_TEMPLATE = "uId:%d;pfh:%d;sum&version";
	public static final String CV_COUNT_TEMPLATE = "uId:%d;count";
	public static final String CV_EXISTS_ID_VERSION_TEMPLATE = "tId:%d;pfh:%d;exists&version";
	
	private TaskRepository repository;
	private TaskMapper mapper;
	private ReactiveCache cache;

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
		return cache.get(CV_EXISTS_ID_VERSION_TEMPLATE.formatted(id, version), Boolean.class)
				.switchIfEmpty(repository.existsByIdAndVersion(id, version)
						.doOnNext(e -> cache.put(CV_EXISTS_ID_VERSION_TEMPLATE.formatted(id, version), e)));
	}
	
	public Mono<Long> sumVersionByIds(Collection<Integer> ids) {
		return repository.sumVersionByids(ids);
	}
	
	public Mono<Long> sumVersionByUser(int userId) {
		return cache.get(CV_SUM_VERSION_TEMPLATE.formatted(userId,-1), Long.class)
				.switchIfEmpty(repository.sumVersionByUser(userId)
						.doOnNext(s -> cache.put(CV_SUM_VERSION_TEMPLATE.formatted(userId, -1), s)));
	}
	
	public Mono<Long> sumVersionByUser(int userId, Pageable pageable, String contains, TaskStatusType status) {
		int pfh = Objects.hash(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				pageable.getSort(),
				contains,
				status
				);
		
		return cache.get(CV_SUM_VERSION_TEMPLATE.formatted(userId), Long.class)
				.switchIfEmpty(repository.sumVersionByUser(userId, pageable, contains, status)
							.doOnNext(s -> cache.put(CV_SUM_VERSION_TEMPLATE.formatted(userId, pfh), s)));
	}
	
	
	public Flux<TaskDTO> findByUser(int id, Pageable pageable, String contains, TaskStatusType status) {
		return findByUser(id, pageable, contains, status, new String[0]);
	}
	
	@SuppressWarnings("unchecked")
	public Flux<TaskDTO> findByUser(int id, Pageable pageable, String contains, TaskStatusType status, String... fields) {
		int pfh = Objects.hash(
					pageable.getPageNumber(),
					pageable.getPageSize(),
					pageable.getSort(),
					contains,
					status,
					fields
					);
		
		return cache.get(CV_FIND_USERID_TEMPLATE.formatted(id, pfh), Collection.class)
				.switchIfEmpty(repository.findAllByUserId(id, pageable, contains, status, fields)
							.map(mapper::toDTO)
							.collectList()
							.doOnNext(dtos -> cache.put(CV_FIND_USERID_TEMPLATE.formatted(id, pfh), dtos))
						)
				.flatMapMany(Flux::fromIterable);
	}

	public Mono<TaskDTO> findById(int taskId) {
		return cache.get(CV_FIND_ID_TEMPLATE.formatted(taskId), TaskDTO.class)
				.switchIfEmpty(repository.findById(taskId)
						.switchIfEmpty(
								Mono.error(new ResourceNotFoundException("Task not found for id equals " + taskId))
								)
						.map(mapper::toDTO)
						.doOnNext(dto -> cache.put(CV_FIND_ID_TEMPLATE.formatted(taskId), dto)));
	}
	
	public Mono<CountDTO> countByUser(int userId) {
		return cache.get(CV_FIND_USERID_TEMPLATE.formatted(userId), Long.class)
				.switchIfEmpty(repository.countByUserId(userId)
						.doOnNext(c -> cache.put(CV_COUNT_TEMPLATE.formatted(userId), c)))
				.map(c -> new CountDTO("task", c));
	}

}
