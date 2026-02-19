package com.lmlasmo.tasklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class TaskStatusService {
	
	private TaskRepository taskRepository;
	private SubtaskRepository subtaskRepository;
	
	public Mono<Void> updateTaskStatus(int taskId, TaskStatusType status) {
		return taskRepository.findStatusSummaryById(taskId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Task not found for id " + taskId)))
				.flatMap(t -> taskRepository.updateStatus(t, status))
				.flatMap(sts -> subtaskRepository.updateStatusByTaskId(taskId, status))
				.as(m -> taskRepository.getOperator().transactional(m));
	}
	
	public Mono<Void> updateSubtaskStatus(TaskStatusType status, List<Integer> subtaskIds) {
		Mono<List<SubtaskSummary.StatusSummary>> statusSummaries = subtaskRepository.findStatusSummaryByIds(subtaskIds)
				.collectList();
		
		Mono<Void> updateSummaries = statusSummaries
				.filter(ss -> ss.size() == subtaskIds.size())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Subtasks not found")))
				.flatMap(ss -> subtaskRepository.updateStatus(ss, status));
		
		Mono<Void> updateTask = statusSummaries
				.flatMapIterable(ss -> ss)
				.map(s -> s.getTaskId())
				.distinct()
				.flatMap(i -> updateTaskStatusFromExistingSubtaskStatus(i, status))
				.then();
		
		return updateSummaries
				.then(updateTask)
				.as(m -> taskRepository.getOperator().transactional(m));
	}
	
	private Mono<Void> updateTaskStatusFromExistingSubtaskStatus(int taskId, TaskStatusType status) {
		return taskRepository.findStatusSummaryById(taskId)
				.flatMap(t -> {
					return assumeTaskStatusFromExistingSubtaskStatus(taskId, status)
							.flatMap(ss -> taskRepository.updateStatus(t, ss))
							.then();
				});
	}
	
	private Mono<TaskStatusType> assumeTaskStatusFromExistingSubtaskStatus(int taskId, TaskStatusType presentStatus) {
		if(presentStatus == TaskStatusType.IN_PROGRESS) {
			return Mono.just(TaskStatusType.IN_PROGRESS);
		}
		
		return subtaskRepository.existsByTaskIdAndStatusNot(taskId, presentStatus)
				.map(ed -> {
					return ed ? TaskStatusType.IN_PROGRESS
							 : presentStatus;
				});
	}

}
