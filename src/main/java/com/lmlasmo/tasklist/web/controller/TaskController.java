package com.lmlasmo.tasklist.web.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.lmlasmo.tasklist.doc.controller.task.CountTasksApiDoc;
import com.lmlasmo.tasklist.doc.controller.task.CreateTaskApiDoc;
import com.lmlasmo.tasklist.doc.controller.task.DeleteTaskApiDoc;
import com.lmlasmo.tasklist.doc.controller.task.FindTaskApiDoc;
import com.lmlasmo.tasklist.doc.controller.task.FindTasksApiDoc;
import com.lmlasmo.tasklist.doc.controller.task.GeneralUpdateTaskApiDoc;
import com.lmlasmo.tasklist.doc.controller.task.UpdateTaskStatusApiDoc;
import com.lmlasmo.tasklist.dto.CountDTO;
import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.security.AuthenticatedResourceAccess;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;
import com.lmlasmo.tasklist.web.util.ETagHelper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
	
	@CreateTaskApiDoc
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<TaskDTO> create(@RequestBody @Valid CreateTaskDTO create) {
		return AuthenticatedTool.getUserId()
				.flatMap(usid -> taskService.save(create, usid));
	}
	
	@DeleteTaskApiDoc
	@DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> delete(ETagHelper etagHelper, @PathVariable @Min(1) int id) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(id, usid))
				.then(etagHelper.checkEtag(et -> taskService.existsByIdAndVersion(id, et)))
				.filter(Boolean::booleanValue)
				.flatMap(c -> taskService.delete(id));
	}
	
	@GeneralUpdateTaskApiDoc
	@PatchMapping(path = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.OK)	
	public Mono<TaskDTO> update(ETagHelper etagHelper, @PathVariable @Min(1) int taskId,
			@RequestBody @Valid UpdateTaskDTO update) {
		
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(taskId, usid))
				.then(etagHelper.checkEtag(et -> taskService.existsByIdAndVersion(taskId, et)))
				.filter(Boolean::booleanValue)
				.flatMap(c -> taskService.update(taskId, update));
	}
	
	@UpdateTaskStatusApiDoc
	@PatchMapping(path = "/{taskId}", params = {"status"})
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> updateTaskStatus(ETagHelper etagHelper, @PathVariable @Min(1) int taskId, 
			@RequestParam @NotNull TaskStatusType status) {
		
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(taskId, usid))
				.then(etagHelper.checkEtag(et -> taskService.existsByIdAndVersion(taskId, et)))
				.filter(Boolean::booleanValue)
				.flatMap(c -> taskStatusService.updateTaskStatus(taskId, status));
	}
	
	@FindTasksApiDoc
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Map<String, Object>> findAllByI(ETagHelper etagHelper,
			@PageableDefault(size = 50) Pageable pageable,
			@RequestParam(value = "contains", required = false) @Size(max = 125) String contains,
			@RequestParam(value = "status", required = false) TaskStatusType status,
			@RequestParam(value = "fields", required = false) Set<String> fields ) {
		
		return AuthenticatedTool.getUserId()
				.flatMapMany(usid -> {
					return etagHelper.checkEtag(et -> taskService.sumVersionByUser(usid, pageable, contains, status).map(s -> s.equals(et)))
							.filter(c -> !c)
							.flatMapMany(c -> taskService.findByUser(usid, pageable, contains, status, fields))
							.as(etagHelper::setETagWithMap);
				});
	}
	
	@FindTaskApiDoc
	@GetMapping(path = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Map<String, Object>> findById(ETagHelper etagHelper, @PathVariable @Min(1) int taskId, 
			@RequestParam(value = "fields", required = false) Set<String> fields) {
		return resourceAccess.canAccess((u, r) -> r.canAccessTask(taskId, u))
				.then(etagHelper.checkEtag(et -> taskService.existsByIdAndVersion(taskId, et)))
				.filter(c -> !c)
				.flatMap(c -> taskService.findById(taskId, fields))
				.as(etagHelper::setETagWithMap);
	}
	
	@CountTasksApiDoc
	@GetMapping(path = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<CountDTO> countByUser() {
		return AuthenticatedTool.getUserId()
				.flatMap(taskService::countByUser);
	}
	
}
