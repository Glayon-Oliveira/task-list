package com.lmlasmo.tasklist.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.controller.util.ETagCheck;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.security.AuthenticatedTool.DirectAuthenticatedTool;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/task")
public class TaskController {

	private TaskService taskService;
	private TaskStatusService taskStatusService;
	
	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)	
	public TaskDTO create(@RequestBody @Valid CreateTaskDTO create) {
		int userId = DirectAuthenticatedTool.getUserId();
		return taskService.save(create, userId).block();
	}	
	
	@DeleteMapping("/{id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessTask(#id, @authTool.getUserId())")
	public Void delete(@PathVariable int id, HttpServletRequest req, HttpServletResponse res) {
		ETagCheck.check(req, res, et -> taskService.existsByIdAndVersion(id, et).block());
		taskService.delete(id).block();
		return null;
	}
	
	@PatchMapping("/{taskId}")
	@ResponseStatus(code = HttpStatus.OK)
	@PreAuthorize("@resourceAccessService.canAccessTask(#taskId, @authTool.getUserId())")
	public TaskDTO update(@PathVariable @Min(1) int taskId, @RequestBody UpdateTaskDTO update, 
			HttpServletRequest req, HttpServletResponse res) {
		
		ETagCheck.check(req, res, et -> taskService.existsByIdAndVersion(taskId, et).block());
		
		return taskService.update(taskId, update).block();
	}
	
	@PatchMapping(path = "/{taskId}", params = {"status"})
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessTask(#taskId, @authTool.getUserId())")
	public Void updateTaskStatus(@PathVariable @Min(1) int taskId, @RequestParam @NotNull TaskStatusType status, 
			HttpServletRequest req, HttpServletResponse res) {
		
		ETagCheck.check(req, res, et -> taskService.existsByIdAndVersion(taskId, et).block());
		
		taskStatusService.updateTaskStatus(taskId, status).block();
		return null;
	}
	
	@GetMapping
	public List<TaskDTO> findAllByI(Pageable pageable, HttpServletRequest req, HttpServletResponse res) {
		int userId = DirectAuthenticatedTool.getUserId();
		
		if(ETagCheck.check(req, res, et -> taskService.sumVersionByUser(userId).block().equals(et))) return null;
		
		return taskService.findByUser(userId).collectList().block();
	}
	
	@GetMapping("/{taskId}")
	@PreAuthorize("@resourceAccessService.canAccessTask(#taskId, @authTool.getUserId())")
	public TaskDTO findById(@PathVariable @Min(1) int taskId, HttpServletRequest req, HttpServletResponse res) {
		
		if(ETagCheck.check(req, res, et -> taskService.existsByIdAndVersion(taskId, et).block())) return null;
		
		return taskService.findById(taskId).block();
	}
	
}
