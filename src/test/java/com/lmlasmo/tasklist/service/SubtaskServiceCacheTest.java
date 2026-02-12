package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.lmlasmo.tasklist.TaskListApplicationTests;
import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@TestInstance(Lifecycle.PER_CLASS)
public class SubtaskServiceCacheTest extends TaskListApplicationTests {
	
	@Autowired
	private SubtaskService subtaskService;
	
	@MockitoBean
	private SubtaskRepository subtaskRepository;
	
	@Test
	void existsByIdAndVersion() throws InterruptedException {
		int subtaskId = 1;
		long version = 1L;
		
		when(subtaskRepository.existsByIdAndVersion(subtaskId, version))
			.thenReturn(Mono.just(true));
		
		boolean noCache = subtaskService.existsByIdAndVersion(subtaskId, version).block();
		Thread.sleep(100);
		
		when(subtaskRepository.existsByIdAndVersion(subtaskId, version))
			.thenReturn(Mono.just(false));
		
		boolean cache = subtaskService.existsByIdAndVersion(subtaskId, version).block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void sumVersionByTask() throws InterruptedException {
		int taskId = 1;
		
		when(subtaskRepository.sumVersionByTask(taskId))
			.thenReturn(Mono.just(1L));
		
		long noCache = subtaskService.sumVersionByTask(taskId).block();
		Thread.sleep(100);
		
		when(subtaskRepository.sumVersionByTask(taskId))
			.thenReturn(Mono.just(2L));
		
		long cache = subtaskService.sumVersionByTask(taskId).block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void sumVersionByTaskWithFilter() throws InterruptedException {
		int taskId = 1;
		Pageable pageable = PageRequest.of(0, 5);
		String contains = "Task name";
		TaskStatusType status = TaskStatusType.COMPLETED;
		
		when(subtaskRepository.sumVersionByTask(taskId, pageable, contains, status))
			.thenReturn(Mono.just(1L));
		
		long noCache = subtaskService.sumVersionByTask(taskId, pageable, contains, status).block();
		Thread.sleep(100);
		
		when(subtaskRepository.sumVersionByTask(taskId, pageable, contains, status))
			.thenReturn(Mono.just(2L));
		
		long cache = subtaskService.sumVersionByTask(taskId, pageable, contains, status).block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void findByTask() throws InterruptedException {
		int taskId = 1;
		Pageable pageable = PageRequest.of(0, 5);
		String contains = "Task name";
		TaskStatusType status = TaskStatusType.COMPLETED;
		String[] fields = new String[] {"name"};
		
		when(subtaskRepository.findAllByTaskId(taskId, pageable, contains, status, fields))
			.thenReturn(Flux.empty());
		
		List<SubtaskDTO> noCache = subtaskService.findByTask(taskId, pageable, contains, status, fields)
				.collectList()
				.block();
		
		Thread.sleep(100);
		
		when(subtaskRepository.findAllByTaskId(taskId, pageable, contains, status, fields))
			.thenReturn(Flux.just(
					new SubtaskSummary(1, null, null, null, null, null, null, null, null, null)
					));
		
		List<SubtaskDTO> cache = subtaskService.findByTask(taskId, pageable, contains, status, fields)
				.collectList()
				.block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void findById() throws InterruptedException {
		int subtaskId = 1;
		
		when(subtaskRepository.existsById(subtaskId)).thenReturn(Mono.just(true));
		
		when(subtaskRepository.findById(subtaskId))
			.thenReturn(Mono.just(new Subtask()));
		
		SubtaskDTO noCache = subtaskService.findById(subtaskId).block();
		
		Thread.sleep(100);
		
		when(subtaskRepository.findById(subtaskId))
			.thenReturn(Mono.just(new Subtask()));
		
		SubtaskDTO cache = subtaskService.findById(subtaskId).block();
		
		assertEquals(noCache, cache);
	}
	
	@Test
	void countByTask() throws InterruptedException {
		int taskId = 1;
		
		when(subtaskRepository.countByTaskId(taskId))
			.thenReturn(Mono.just(1L));
		
		long noCache = subtaskService.countByTask(taskId)
				.block()
				.getTotal();
		
		Thread.sleep(100);
		
		when(subtaskRepository.countByTaskId(taskId))
			.thenReturn(Mono.just(2L));
		
		long cache = subtaskService.countByTask(taskId)
				.block()
				.getTotal();
		
		assertEquals(noCache, cache);
	}
	
}
