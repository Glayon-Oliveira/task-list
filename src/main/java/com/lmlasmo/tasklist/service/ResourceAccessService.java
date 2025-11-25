package com.lmlasmo.tasklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.repository.UserEmailRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class ResourceAccessService {
	
	private UserEmailRepository emailRepository;
	private TaskRepository taskRepository;
	private SubtaskRepository subtaskRepository;
	
	public Mono<Void> canAccessEmail(int emailId, int userId) {
		return emailRepository.existsByIdAndUserId(emailId, userId)
				.filter(Boolean::booleanValue)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Email not found for id " + emailId)))
				.then();
	}
	
	public Mono<Void> canAccessEmail(String email, int userId) {
		return emailRepository.existsByEmailAndUserId(email, userId)
				.filter(Boolean::booleanValue)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Email not found")))
				.then();
	}
	
	public Mono<Void> canAccessTask(int taskId, int userId) {
		return taskRepository.existsByIdAndUserId(taskId, userId)
				.filter(Boolean::booleanValue)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Task not found for id " + taskId)))
				.then();
	}
	
	public Mono<Void> canAccessSubtask(int subtaskId, int userId) {
		return subtaskRepository.existsByIdAndTaskUserId(subtaskId, userId)
				.filter(Boolean::valueOf)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subask not found for id " + subtaskId)))
				.then();
	}
	
	public Mono<Void> canAccessSubtask(List<Integer> subtaskIds, int userId) {
		return subtaskRepository.countByIdInAndTaskUserId(subtaskIds, userId)
				.map(c -> subtaskIds.size() == c)
				.filter(Boolean::booleanValue)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subask not found")))
				.then();
	}
	
}
