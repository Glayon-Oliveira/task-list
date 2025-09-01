package com.lmlasmo.tasklist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.advice.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.repository.TaskRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TaskService {
	
	private TaskRepository repository;

	public TaskDTO save(TaskDTO create) {		
		Task task = new Task(create);
		return new TaskDTO(repository.save(task));
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
