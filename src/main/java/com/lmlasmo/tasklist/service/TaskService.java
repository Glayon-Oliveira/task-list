package com.lmlasmo.tasklist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateDeadlineTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateDescriptionTaskDTO;
import com.lmlasmo.tasklist.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.TaskRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TaskService {
	
	private TaskRepository repository;

	public TaskDTO save(CreateTaskDTO create, int userId) {		
		Task task = new Task(create, new User(userId));
		return new TaskDTO(repository.save(task), true);
	}
	
	public void delete(int id) {
		if(!repository.existsById(id)) throw new EntityNotFoundException("Task not found");
		
		repository.deleteById(id);
		
		if(repository.existsById(id)) throw new EntityNotDeleteException("Task not delete");
	}
	
	public TaskDTO updateDescription(@Min(1) int taskId, UpdateDescriptionTaskDTO update) {
		Task task = repository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task not found for id equals " + taskId));
		
		if(update.getName() != null) task.setName(update.getName());
		if(update.getSummary() != null) task.setSummary(update.getSummary());
		
		return new TaskDTO(repository.save(task));		
	}
	
	public TaskDTO updateDeadline(@Min(1) int taskId, UpdateDeadlineTaskDTO update) {
		Task task = repository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task not found for id equals " + taskId));
		
		task.setDeadline(update.getDeadline().toInstant());
		task.setDeadlineZone(update.getDeadlineZone());
		
		return new TaskDTO(repository.save(task));
	}
	
	public Page<TaskDTO> findByUser(int id, boolean withSubtasks, Pageable pageable) {
		return repository.findByUserId(id, pageable).map(t -> new TaskDTO(t, withSubtasks));
	}
	
	public Page<TaskDTO> findAll(Pageable pageable){
		return repository.findAll(pageable).map(TaskDTO::new);
	}

	public TaskDTO findById(int taskId, boolean withSubtasks) {
		return repository.findById(taskId).map(t -> new TaskDTO(t, withSubtasks))
				.orElseThrow(() -> new EntityNotFoundException("Task not found for id equals " + taskId));
	}

}
