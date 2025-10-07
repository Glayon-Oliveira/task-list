package com.lmlasmo.tasklist.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.exception.TaskHasSubtasksException;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.StatusSummary;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TaskStatusService {
	
	private TaskRepository taskRepository;	
	private SubtaskRepository subtaskRepository;
	
	public void updateTaskStatus(int taskId, TaskStatusType status) {
		if(!taskRepository.existsById(taskId)) throw new EntityNotFoundException("Task not found for id " + taskId);
		
		if(subtaskRepository.countByTaskId(taskId) > 0) throw new TaskHasSubtasksException("Status of Task has subtasks is defined by subtasks");
		
		taskRepository.updateStatus(taskId, status);
	}

	@Transactional
	public void updateSubtaskStatus(TaskStatusType status, List<Integer> subtaskIds) {		
		List<StatusSummary> stIdStatusTasks = subtaskRepository.findStatusSummaryByIds(subtaskIds);
		
		if(stIdStatusTasks.size() < subtaskIds.size()) throw new EntityNotFoundException("Subtasks not found");
		
		stIdStatusTasks = stIdStatusTasks.stream()
				.filter(s -> s.getStatus().equals(status))
				.toList();
		
		if(stIdStatusTasks.size() > 1) {
			subtaskRepository.updateStatus(subtaskIds, status);
		}else {
			subtaskRepository.updateStatus(stIdStatusTasks.get(0).getId(), status);
		}
		
		stIdStatusTasks.stream()
			.map(s -> s.getTaskId())
			.distinct()
			.forEach(i -> updateTaskStatus(i));
	}
	
	private void updateTaskStatus(int taskId) {
		if(subtaskRepository.existsByTaskIdAndStatus(taskId, TaskStatusType.IN_PROGRESS)) {
			taskRepository.updateStatus(taskId, TaskStatusType.IN_PROGRESS);
		}else if(subtaskRepository.existsByTaskIdAndStatus(taskId, TaskStatusType.COMPLETED)){
			taskRepository.updateStatus(taskId, TaskStatusType.COMPLETED);
		}else {
			taskRepository.updateStatus(taskId, TaskStatusType.PENDING);
		}
	}

}
