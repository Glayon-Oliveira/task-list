package com.lmlasmo.tasklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.TaskRepository;

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
		return subtaskRepository.existsByIdAndTaskUserId(subtaskId, userId);
	}
	
	public boolean canAccessSubtask(List<Integer> subtaskIds, int userId) {
		long count = subtaskRepository.countByIdInAndTaskUserId(subtaskIds, userId);
		return subtaskIds.size() == count;
	}
	
}
