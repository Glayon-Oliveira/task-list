package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.lmlasmo.tasklist.TaskListApplicationTests;
import com.lmlasmo.tasklist.cache.ReactiveCache;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@TestInstance(Lifecycle.PER_CLASS)
public class TaskServiceCacheTest extends TaskListApplicationTests {
	
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
		Thread.sleep(100);
		
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
		Thread.sleep(100);
		
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
		Thread.sleep(100);
		
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
		String[] fields = new String[] {"name"};
		
		when(taskRepository.findAllByUserId(userId, pageable, contains, status, fields))
			.thenReturn(Flux.empty());
		
		List<TaskDTO> noCache = taskService.findByUser(userId, pageable, contains, status, fields)
				.collectList()
				.block();
		
		Thread.sleep(100);
		
		when(taskRepository.findAllByUserId(userId, pageable, contains, status, fields))
			.thenReturn(Flux.just(
					new TaskSummary(1, null, null, null, null, null, null, null, null, null)
					));
		
		List<TaskDTO> cache = taskService.findByUser(userId, pageable, contains, status, fields)
				.collectList()
				.block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void findById() throws InterruptedException {
		int taskId = 1;
		
		when(taskRepository.existsById(taskId)).thenReturn(Mono.just(true));
		
		when(taskRepository.findById(taskId))
			.thenReturn(Mono.just(new Task()));
		
		TaskDTO noCache = taskService.findById(taskId).block();
		
		Thread.sleep(100);
		
		when(taskRepository.findById(taskId))
			.thenReturn(Mono.just(new Task()));
		
		TaskDTO cache = taskService.findById(taskId).block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void countByUser() throws InterruptedException {
		int userId = 1;
		
		when(taskRepository.countByUserId(userId))
			.thenReturn(Mono.just(1L));
		
		long noCache = taskService.countByUser(userId)
				.block()
				.getTotal();
		
		Thread.sleep(100);
		
		when(taskRepository.countByUserId(userId))
			.thenReturn(Mono.just(2L));
		
		long cache = taskService.countByUser(userId)
				.block()
				.getTotal();
		
		assertEquals(noCache, cache);
	}
	
}
