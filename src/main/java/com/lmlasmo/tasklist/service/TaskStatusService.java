package com.lmlasmo.tasklist.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.exception.TaskHasSubtasksException;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TaskStatusService {
	
	private TaskRepository taskRepository;	
	private SubtaskRepository subtaskRepository;
	
	public void updateTaskStatus(int taskId, TaskStatusType status) {
		TaskSummary.StatusSummary summary = taskRepository.findStatusSummaryById(taskId)
				.orElseThrow(() -> new EntityNotFoundException("Task not found for id " + taskId));		
		
		if(subtaskRepository.countByTaskId(taskId) > 0) throw new TaskHasSubtasksException("Status of Task has subtasks is defined by subtasks");
		
		taskRepository.updateStatus(summary, status);
	}

	@Transactional
	public void updateSubtaskStatus(TaskStatusType status, List<Integer> subtaskIds) {		
		List<SubtaskSummary.StatusSummary> stIdStatusTasks = subtaskRepository.findStatusSummaryByIds(subtaskIds);
		
		if(stIdStatusTasks.size() < subtaskIds.size()) throw new EntityNotFoundException("Subtasks not found");
		
		stIdStatusTasks = stIdStatusTasks.stream()
				.filter(s -> !s.getStatus().equals(status))
				.toList();
		
		subtaskRepository.updateStatus(stIdStatusTasks, status);
		
		stIdStatusTasks.stream()
			.map(s -> s.getTaskId())
			.distinct()
			.forEach(i -> updateTaskStatus(i));
	}
	
	private void updateTaskStatus(int taskId) {
		TaskSummary.StatusSummary summary = taskRepository.findStatusSummaryById(taskId)
				.orElseThrow(() -> new EntityNotFoundException("Task not found for id " + taskId));
		
		if(summary.getStatus().equals(TaskStatusType.IN_PROGRESS)) {
			taskRepository.updateStatus(summary, TaskStatusType.IN_PROGRESS);
		}else if(summary.getStatus().equals(TaskStatusType.COMPLETED)){
			taskRepository.updateStatus(summary, TaskStatusType.COMPLETED);
		}else {
			taskRepository.updateStatus(summary, TaskStatusType.PENDING);
		}
	}

}
