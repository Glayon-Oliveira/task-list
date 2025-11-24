package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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

import com.lmlasmo.tasklist.controller.util.ETagHelper;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.security.AuthenticatedResourceAccess;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/api/task")
@Validated
public class TaskController {

	private TaskService taskService;
	private TaskStatusService taskStatusService;
	private	AuthenticatedResourceAccess resourceAccess;
	
	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<TaskDTO> create(@RequestBody @Valid CreateTaskDTO create) {
		return AuthenticatedTool.getUserId()
				.flatMap(usid -> taskService.save(create, usid));
	}	
	
	@DeleteMapping("/{id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> delete(@PathVariable @Min(1) int id) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(id, usid))
				.then(ETagHelper.checkEtag(et -> taskService.existsByIdAndVersion(id, et)))
				.filter(Boolean::booleanValue)
				.flatMap(c -> taskService.delete(id));
	}
	
	@PatchMapping("/{taskId}")
	@ResponseStatus(code = HttpStatus.OK)	
	public Mono<TaskDTO> update(@PathVariable @Min(1) int taskId, @RequestBody @Valid UpdateTaskDTO update) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(taskId, usid))
				.then(ETagHelper.checkEtag(et -> taskService.existsByIdAndVersion(taskId, et)))
				.filter(Boolean::booleanValue)
				.flatMap(c -> taskService.update(taskId, update));
	}
	
	@PatchMapping(path = "/{taskId}", params = {"status"})
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> updateTaskStatus(@PathVariable @Min(1) int taskId, @RequestParam @NotNull TaskStatusType status) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(taskId, usid))
				.then(ETagHelper.checkEtag(et -> taskService.existsByIdAndVersion(taskId, et)))
				.filter(Boolean::booleanValue)
				.flatMap(c -> taskStatusService.updateTaskStatus(taskId, status));
	}
	
	@GetMapping
	public Flux<TaskDTO> findAllByI() {
		return AuthenticatedTool.getUserId()
				.flatMapMany(usid -> {
					return ETagHelper.checkEtag(et -> taskService.sumVersionByUser(usid).map(s -> s.equals(et)))
							.filter(Boolean::valueOf)
							.flatMapMany(c -> taskService.findByUser(usid))
							.as(ETagHelper::setEtag);
				});
	}
	
	@GetMapping("/{taskId}")
	public Mono<TaskDTO> findById(@PathVariable @Min(1) int taskId) {
		return resourceAccess.canAccess((u, r) -> r.canAccessTask(taskId, u))
				.then(ETagHelper.checkEtag(et -> taskService.existsByIdAndVersion(taskId, et)))
				.filter(Boolean::valueOf)
				.flatMap(c -> taskService.findById(taskId))
				.as(ETagHelper::setEtag);
	}
	
}
