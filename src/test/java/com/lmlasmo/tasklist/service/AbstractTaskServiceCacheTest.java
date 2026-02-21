package com.lmlasmo.tasklist.service;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.lmlasmo.tasklist.cache.ReactiveCache;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.repository.summary.Field;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractTaskServiceCacheTest {

	@Autowired
	private TaskService taskService;
	
	@MockitoBean
	private TaskRepository taskRepository;
	
	@Autowired
	private ReactiveCache cache;
	
	@AfterEach
	protected void clear() throws InterruptedException {
		cache.clear();
		Thread.sleep(500);
	}
	
	@Test
	void existsByIdAndVersion() throws InterruptedException {
		int taskId = 1;
		long version = 1L;
		
		when(taskRepository.existsByIdAndVersion(taskId, version))
			.thenReturn(Mono.just(true));
		
		boolean noCache = taskService.existsByIdAndVersion(taskId, version).block();
		Thread.sleep(500);
		
		when(taskRepository.existsByIdAndVersion(taskId, version))
			.thenReturn(Mono.just(false));
		
		boolean cache = taskService.existsByIdAndVersion(taskId, version).block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void sumVersionByUser() throws InterruptedException {
		int userId = 1;
		
		when(taskRepository.sumVersionByUser(userId))
			.thenReturn(Mono.just(1L));
		
		long noCache = taskService.sumVersionByUser(userId).block();
		Thread.sleep(500);
		
		when(taskRepository.sumVersionByUser(userId))
			.thenReturn(Mono.just(2L));
		
		long cache = taskService.sumVersionByUser(userId).block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void sumVersionByUserWithFilter() throws InterruptedException {
		int userId = 1;
		Pageable pageable = PageRequest.of(0, 5);
		String contains = "Task name";
		TaskStatusType status = TaskStatusType.COMPLETED;
		
		when(taskRepository.sumVersionByUser(userId, pageable, contains, status))
			.thenReturn(Mono.just(1L));
		
		long noCache = taskService.sumVersionByUser(userId, pageable, contains, status).block();
		Thread.sleep(500);
		
		when(taskRepository.sumVersionByUser(userId, pageable, contains, status))
			.thenReturn(Mono.just(2L));
		
		long cache = taskService.sumVersionByUser(userId, pageable, contains, status).block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void findByUser() throws InterruptedException {
		int userId = 1;
		Pageable pageable = PageRequest.of(0, 5);
		String contains = "Task name";
		TaskStatusType status = TaskStatusType.COMPLETED;
		Set<String> fields = Set.of("name");
		
		when(taskRepository.findSummariesByUserId(userId, pageable, contains, status, fields))
			.thenReturn(Flux.just(
					new TaskSummary(
							Field.of(1), Field.absent(), Field.absent(), Field.absent(), Field.absent(), 
							Field.absent(), Field.absent(), Field.absent(), Field.absent(), Field.absent()
							)
					));
		
		List<Map<String, Object>> noCache = taskService.findByUser(userId, pageable, contains, status, fields)
				.collectList()
				.block();
		
		Thread.sleep(500);
		
		when(taskRepository.findSummariesByUserId(userId, pageable, contains, status, fields))
			.thenReturn(Flux.just(
					new TaskSummary(
							Field.of(2), Field.of("no cache"), Field.absent(), Field.absent(), Field.absent(), 
							Field.absent(), Field.absent(), Field.absent(), Field.absent(), Field.absent()
							)
					));
		
		List<Map<String, Object>> cache = taskService.findByUser(userId, pageable, contains, status, fields)
				.collectList()
				.block();
		
		assertEquals(noCache.get(0).get("id"), cache.get(0).get("id"));
		assertNull(cache.get(0).get("name"));
	}
	
	@Test
	void findById() throws InterruptedException {
		int taskId = 1;
		
		when(taskRepository.existsById(taskId)).thenReturn(Mono.just(true));
		
		when(taskRepository.findById(taskId))
			.thenReturn(Mono.just(new Task(1)));
		
		TaskDTO noCache = taskService.findById(taskId).block();
		
		Thread.sleep(500);
		
		when(taskRepository.findById(taskId))
			.thenReturn(Mono.just(new Task()));
		
		TaskDTO cache = taskService.findById(taskId).block();
		
		assertEquals(noCache.getId(), cache.getId());
	}
	
	@Test
	void findByIdWithFields() throws InterruptedException {
		int taskId = 1;
		
		when(taskRepository.existsById(taskId)).thenReturn(Mono.just(true));
		
		when(taskRepository.findSummaryById(taskId, Set.of("name")))
			.thenReturn(Mono.just(
					new TaskSummary(
							Field.of(1), Field.of("name"), Field.absent(), Field.absent(), Field.absent(), 
							Field.absent(), Field.absent(), Field.absent(), Field.absent(), Field.absent())
					));
		
		Map<String, Object> noCache = taskService.findById(taskId, Set.of("name")).block();
		
		Thread.sleep(500);
		
		when(taskRepository.findSummaryById(taskId, Set.of("name")))
			.thenReturn(Mono.empty());
		
		Map<String, Object> cache = taskService.findById(taskId, Set.of("name")).block();
		
		assertEquals(noCache.get("id"), cache.get("id"));
		assertEquals(noCache.get("name"), cache.get("name"));
		assertNull(cache.get("summary"));
	}
	
	@Test
	void countByUser() throws InterruptedException {
		int userId = 1;
		
		when(taskRepository.countByUserId(userId))
			.thenReturn(Mono.just(1L));
		
		long noCache = taskService.countByUser(userId)
				.block()
				.getTotal();
		
		Thread.sleep(500);
		
		when(taskRepository.countByUserId(userId))
			.thenReturn(Mono.just(2L));
		
		long cache = taskService.countByUser(userId)
				.block()
				.getTotal();
		
		assertEquals(noCache, cache);
	}
	
}
