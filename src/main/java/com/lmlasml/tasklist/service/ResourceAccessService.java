package com.lmlasml.tasklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lmlasml.tasklist.repository.SubtaskRepository;
import com.lmlasml.tasklist.repository.TaskRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ResourceAccessService {
	
	private TaskRepository taskRepository;
	private SubtaskRepository subtaskRepository;
	
	public boolean canAccessTask(int taskId, int userId) {
		return taskRepository.existsByIdAndUserId(taskId, userId);
	}
	
	public boolean canAccessSubtask(int subtaskId, int userId) {
		return subtaskRepository.existsByIdInAndTaskUserId(List.of(subtaskId), userId);
	}
	
	public boolean canAccessSubtask(List<Integer> subtaskIds, int userId) {
		return subtaskRepository.existsByIdInAndTaskUserId(subtaskIds, userId);
	}
	
}
