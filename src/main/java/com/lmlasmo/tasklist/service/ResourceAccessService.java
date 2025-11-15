package com.lmlasmo.tasklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.repository.UserEmailRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ResourceAccessService {
	
	private UserEmailRepository emailRepository;
	private TaskRepository taskRepository;
	private SubtaskRepository subtaskRepository;
	
	public boolean canAccessEmail(int emailId, int userId) {
		return emailRepository.existsByIdAndUserId(emailId, userId).block();
	}
	
	public boolean canAccessEmail(String email, int userId) {
		return emailRepository.existsByEmailAndUserId(email, userId).block();
	}
	
	public boolean canAccessTask(int taskId, int userId) {
		return taskRepository.existsByIdAndUserId(taskId, userId).block();
	}
	
	public boolean canAccessSubtask(int subtaskId, int userId) {
		return subtaskRepository.existsByIdAndTaskUserId(subtaskId, userId).block();
	}
	
	public boolean canAccessSubtask(List<Integer> subtaskIds, int userId) {
		long count = subtaskRepository.countByIdInAndTaskUserId(subtaskIds, userId).block();
		return subtaskIds.size() == count;
	}
	
}
