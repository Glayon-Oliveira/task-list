package com.lmlasmo.tasklist.data.tool;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.TaskRepository;

import jakarta.annotation.PreDestroy;

@Component
@Lazy
public class TaskTestTool {

	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private UserTestTool userTestTool;
	
	@PreDestroy
	protected void destroy() {
		taskRepository.deleteAll().block();
	}
	
	public void runWithUser(User user, Consumer<Task> consumerTask) {
		Task task = generateTaskEntity(user);
		consumerTask.accept(task);
		
		taskRepository.deleteAll().block();
	}
	
	public void runWithUser(User user, int quantity, Consumer<Set<Task>> consumerNTasks) {
		Set<Task> tasks = new LinkedHashSet<>();
		
		for(int cc = 0; cc < quantity; cc++) {
			Task task = generateTaskEntity(user);
			tasks.add(task);
		}
		
		consumerNTasks.accept(tasks);
		
		taskRepository.deleteAll().block();
	}
	
	public void runWithUniqueTask(Consumer<Task> consumerTask) {
		userTestTool.runWithUniqueUser(u -> {
			Task task = generateTaskEntity(u);
			consumerTask.accept(task);
		});
		
		taskRepository.deleteAll().block();
	}
	
	public void runWithNTasks(long quantity, Consumer<Set<Task>> consumerNTasks) {
		userTestTool.runWithUniqueUser(u -> {
			Set<Task> tasks = new LinkedHashSet<>();
			
			for(int cc = 0; cc < quantity; cc++) {
				Task task = generateTaskEntity(u);
				tasks.add(task);
			}
			
			consumerNTasks.accept(tasks);
		});
		
		taskRepository.deleteAll().block();
	}
	
	private Task generateTaskEntity(User user) {
		Task task = new Task();
		task.setName("Task ID=" + UUID.randomUUID());
		task.setSummary(("Task ID=" + UUID.randomUUID()));
		task.setDeadline(Instant.now().plus(Duration.ofMinutes(10)));
		task.setDeadlineZone(ZoneOffset.UTC.toString());
		task.setUserId(user.getId());
		
		return taskRepository.save(task).block();
	}
	
}
