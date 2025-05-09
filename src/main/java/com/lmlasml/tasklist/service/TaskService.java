package com.lmlasml.tasklist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lmlasml.tasklist.advice.exception.EntityNotDeleteException;
import com.lmlasml.tasklist.dto.TaskDTO;
import com.lmlasml.tasklist.dto.TaskStatusDTO;
import com.lmlasml.tasklist.model.Task;
import com.lmlasml.tasklist.repository.TaskRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Service
public class TaskService {
	
	private TaskRepository repository;

	public TaskDTO save(TaskDTO create) {		
		Task task = new Task(create);
		repository.save(task);
		return new TaskDTO(task);
	}
	
	public TaskDTO update(TaskStatusDTO status) {		
		Task task = repository.findById(status.getId()).orElseGet(() -> null);
		
		if(task == null) throw new EntityNotFoundException("Task not found");
		
		task.setStatus(status.getStatus());
		task = repository.save(task);
		return new TaskDTO(task);
	}
	
	public void delete(int id) {
		if(!repository.existsById(id)) throw new EntityNotFoundException("Task not found");
		
		repository.deleteById(id);
		
		if(repository.existsById(id)) throw new EntityNotDeleteException("Task not delete");
	}
	
	public Page<TaskDTO> findByUser(int id, Pageable pageable){
		return repository.findByUserId(id, pageable).map(TaskDTO::new);
	}
	
	public Page<TaskDTO> findAll(Pageable pageable){
		return repository.findAll(pageable).map(TaskDTO::new);
	}

}
