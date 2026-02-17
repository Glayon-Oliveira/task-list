package com.lmlasmo.tasklist.data.tool;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.SubtaskRepository;

import jakarta.annotation.PreDestroy;

@Component
@Lazy
public class SubtaskTestTool {

	@Autowired
	private SubtaskRepository subtaskRepository;
	
	@Autowired
	private TaskTestTool taskTestTool;
	
	@PreDestroy
	protected void destroy() {
		subtaskRepository.deleteAll().block();
	}
	
	public void runWithUser(User user, Consumer<Subtask> consumerSubtask) {
		taskTestTool.runWithUser(user, t -> {
			Subtask subtask = generateSubtaskEntity(t);
			consumerSubtask.accept(subtask);
		});
		
		subtaskRepository.deleteAll().block();
	}
	
	public void runWithUser(User user,  int quantity, Consumer<Set<Subtask>> consumerSubtasks) {
		taskTestTool.runWithUser(user, t -> {
			Set<Subtask> subtasks = new LinkedHashSet<>();
			
			for(int cc = 0; cc < quantity; cc++) {
				Subtask subtask = generateSubtaskEntity(t);
				subtasks.add(subtask);
			}
			
			consumerSubtasks.accept(subtasks);
		});
		
		subtaskRepository.deleteAll().block();
	}
	
	public void runWithTask(Task task, Consumer<Subtask> consumerSubtask) {
		Subtask subtask = generateSubtaskEntity(task);
		consumerSubtask.accept(subtask);
		
		subtaskRepository.deleteAll().block();
	}
	
	public void runWithTask(Task task,  int quantity, Consumer<Set<Subtask>> consumerSubtasks) {
		Set<Subtask> subtasks = new LinkedHashSet<>();
		
		for(int cc = 0; cc < quantity; cc++) {
			Subtask subtask = generateSubtaskEntity(task);
			subtasks.add(subtask);
		}
		
		consumerSubtasks.accept(subtasks);
		
		subtaskRepository.deleteAll().block();
	}
	
	public void runWithUniqueSubtask(Consumer<Subtask> consumerSubtask) {
		taskTestTool.runWithUniqueTask(t -> {
			Subtask subtask = generateSubtaskEntity(t);
			consumerSubtask.accept(subtask);
		});
		
		subtaskRepository.deleteAll().block();
	}
	
	public void runWithNSubtask(int quantity, Consumer<Set<Subtask>> consumerSubtasks) {
		taskTestTool.runWithUniqueTask(t -> {
			Set<Subtask> subtasks = new LinkedHashSet<>();
			
			for(int cc = 0; cc < quantity; cc++) {
				Subtask subtask = generateSubtaskEntity(t);
				subtasks.add(subtask);
			}
			
			consumerSubtasks.accept(subtasks);
		});
		
		subtaskRepository.deleteAll().block();
	}
	
	private Subtask generateSubtaskEntity(Task task) {
		Subtask subtask = new Subtask();
		subtask.setName("Subtask ID=" + UUID.randomUUID());
		subtask.setSummary(("Subtask ID=" + UUID.randomUUID()));
		subtask.setDurationMinutes(new Random().nextInt(5, 20));
		subtask.setTaskId(task.getId());
		subtask.setPosition(BigDecimal.valueOf(
				new Random()
					.nextDouble(0, Math.pow(10, 10))
				));
		
		return subtaskRepository.save(subtask).block();
	}
	
	
}
