package com.lmlasmo.tasklist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.service.applier.UpdateTaskApplier;

import jakarta.persistence.EntityNotFoundException;
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
	
	public TaskDTO update(int taskId, UpdateTaskDTO update) {
		Task task = repository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task not found for id equals " + taskId));
		
		UpdateTaskApplier.apply(update, task);
		
		return new TaskDTO(repository.save(task));
	}	
	
	public boolean existsByIdAndVersion(int id, long version) {
		return repository.existsByIdAndVersion(id, version);
	}
	
	public long sumVersionByIds(Iterable<Integer> ids) {
		return repository.sumVersionByids(ids);
	}
	
	public long sumVersionByUser(int userId) {
		return repository.sumVersionByUser(userId);
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
