package com.lmlasml.tasklist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasml.tasklist.dto.TaskDTO;
import com.lmlasml.tasklist.dto.create.CreateTaskDTO;
import com.lmlasml.tasklist.model.TaskStatusType;
import com.lmlasml.tasklist.security.AuthenticatedTool;
import com.lmlasml.tasklist.service.TaskService;
import com.lmlasml.tasklist.service.TaskStatusService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/task")
public class TaskController {

	private TaskService taskService;
	private TaskStatusService taskStatusService;
	
	@PostMapping("/")
	@ResponseStatus(code = HttpStatus.CREATED)	
	public TaskDTO create(@RequestBody @Valid CreateTaskDTO create) {
		int userId = AuthenticatedTool.getUserId();
		return taskService.save(create, userId);
	}	
	
	@DeleteMapping("/{id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessTask(#id, authentication.principal.id)")
	public Void delete(@PathVariable int id) {
		taskService.delete(id);
		return null;
	}
	
	@PutMapping(params = {"taskId", "status"})
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessTask(#taskId, authentication.principal.id)")
	public Void updateTaskStatus(@RequestParam @Min(1) int taskId, @RequestParam @NotNull TaskStatusType status) {
		taskStatusService.updateTaskStatus(taskId, status);
		return null;
	}
	
	@GetMapping("/")
	public Page<TaskDTO> findAllByI(Pageable pageable){
		return taskService.findByUser(AuthenticatedTool.getUserId(), pageable);
	}
	
}
