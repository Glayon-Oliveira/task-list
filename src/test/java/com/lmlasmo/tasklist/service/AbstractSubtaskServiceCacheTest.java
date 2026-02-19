package com.lmlasmo.tasklist.service;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lmlasmo.tasklist.cache.CacheCaffeineProperties;
import com.lmlasmo.tasklist.cache.CacheConf;
import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.summary.Field;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@Import({
    SubtaskService.class,
    CacheConf.class,
})
@ComponentScan(basePackages = "com.lmlasmo.tasklist.mapper")
@EnableConfigurationProperties(CacheCaffeineProperties.class)
@TestPropertySource(locations = "classpath:application.properties")
public abstract class AbstractSubtaskServiceCacheTest {
	
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
		Thread.sleep(500);
		
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
		Thread.sleep(500);
		
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
		Thread.sleep(500);
		
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
		.thenReturn(Flux.just(
				new SubtaskSummary(
						Field.of(1), Field.absent(), Field.absent(), Field.absent(), Field.absent(), 
						Field.absent(), Field.absent(), Field.absent(), Field.absent(), Field.absent()
						)
				));
		
		List<Map<String, Object>> noCache = subtaskService.findByTask(taskId, pageable, contains, status, fields)
				.collectList()
				.block();
		
		Thread.sleep(500);
		
		when(subtaskRepository.findAllByTaskId(taskId, pageable, contains, status, fields))
		.thenReturn(Flux.just(
				new SubtaskSummary(
						Field.of(1), Field.of("no cache"), Field.absent(), Field.absent(), Field.absent(), 
						Field.absent(), Field.absent(), Field.absent(), Field.absent(), Field.absent()
						)
				));
		
		List<Map<String, Object>> cache = subtaskService.findByTask(taskId, pageable, contains, status, fields)
				.collectList()
				.block();
		
		assertEquals(noCache.get(0).get("id"), cache.get(0).get("id"));
		assertNull(cache.get(0).get("name"));
	}
	
	@Test
	void findById() throws InterruptedException {
		int subtaskId = 1;
		
		when(subtaskRepository.existsById(subtaskId)).thenReturn(Mono.just(true));
		
		when(subtaskRepository.findById(subtaskId))
			.thenReturn(Mono.just(new Subtask()));
		
		SubtaskDTO noCache = subtaskService.findById(subtaskId).block();
		
		Thread.sleep(500);
		
		when(subtaskRepository.findById(subtaskId))
			.thenReturn(Mono.just(new Subtask()));
		
		SubtaskDTO cache = subtaskService.findById(subtaskId).block();
		
		assertEquals(noCache.getId(), cache.getId());
	}
	
	@Test
	void countByTask() throws InterruptedException {
		int taskId = 1;
		
		when(subtaskRepository.countByTaskId(taskId))
			.thenReturn(Mono.just(1L));
		
		long noCache = subtaskService.countByTask(taskId)
				.block()
				.getTotal();
		
		Thread.sleep(500);
		
		when(subtaskRepository.countByTaskId(taskId))
			.thenReturn(Mono.just(2L));
		
		long cache = subtaskService.countByTask(taskId)
				.block()
				.getTotal();
		
		assertEquals(noCache, cache);
	}
	
}
