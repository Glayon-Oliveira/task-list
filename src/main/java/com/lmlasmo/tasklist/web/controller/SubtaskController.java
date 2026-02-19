package com.lmlasmo.tasklist.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
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

import com.lmlasmo.tasklist.doc.controller.subtask.CountSubtasksApiDoc;
import com.lmlasmo.tasklist.doc.controller.subtask.CreateSubtaskApiDoc;
import com.lmlasmo.tasklist.doc.controller.subtask.DeleteSubtaskApiDoc;
import com.lmlasmo.tasklist.doc.controller.subtask.FindSubtaskApiDoc;
import com.lmlasmo.tasklist.doc.controller.subtask.FindSubtasksApiDoc;
import com.lmlasmo.tasklist.doc.controller.subtask.GeneralUpdateSubtaskApiDoc;
import com.lmlasmo.tasklist.doc.controller.subtask.UpdateSubtaskPositionApiDoc;
import com.lmlasmo.tasklist.doc.controller.subtask.UpdateSubtaskStatusApiDoc;
import com.lmlasmo.tasklist.dto.CountDTO;
import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskPositionDTO;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.security.AuthenticatedResourceAccess;
import com.lmlasmo.tasklist.service.SubtaskService;
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
@RequestMapping("/api/subtask")
@Validated
public class SubtaskController {
	
	private SubtaskService subtaskService;
	private TaskStatusService taskStatusService;
	private	AuthenticatedResourceAccess resourceAccess;
	
	@CreateSubtaskApiDoc
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<SubtaskDTO> create(@RequestBody @Valid CreateSubtaskDTO create) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(create.getTaskId(), usid))
				.then(subtaskService.save(create));
	}
	
	@DeleteSubtaskApiDoc
	@DeleteMapping(params = "subtaskIds")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> delete(ETagHelper etagHelper, @RequestParam List<@Min(1) Integer> subtaskIds) {		
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskIds, usid))
				.then(etagHelper.checkEtag(et -> subtaskService.sumVersionByIds(subtaskIds).map(s -> s == et)))
				.filter(Boolean::booleanValue)
				.thenEmpty(subtaskService.delete(subtaskIds));
	}
	
	@GeneralUpdateSubtaskApiDoc
	@PatchMapping(path = "/{subtaskId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<SubtaskDTO> update(ETagHelper etagHelper, @PathVariable @Min(1) int subtaskId, 
			@RequestBody @Valid UpdateSubtaskDTO update) {
		
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskId, usid))
				.then(etagHelper.checkEtag(et -> subtaskService.existsByIdAndVersion(subtaskId, et)))
				.filter(Boolean::booleanValue)
				.then(subtaskService.update(subtaskId, update));
	}

	@UpdateSubtaskPositionApiDoc
	@PatchMapping(path = "/{subtaskId}/position")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> updateSubtaskPosition(ETagHelper etagHelper, @PathVariable @Min(1) int subtaskId,
			@RequestBody @Valid UpdateSubtaskPositionDTO update) {
		
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskId, usid))
				.then(resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(update.getAnchorSubtaskId(), usid)))
				.then(etagHelper.checkEtag(et -> subtaskService.existsByIdAndVersion(subtaskId, et)))
				.filter(Boolean::booleanValue)
				.thenEmpty(subtaskService.updatePosition(subtaskId, update));
	}
	
	@UpdateSubtaskStatusApiDoc
	@PatchMapping(params = {"subtaskIds", "status"})
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> updateSubtaskStatus(ETagHelper etagHelper,
			@RequestParam List<@Min(1) Integer> subtaskIds,
			@RequestParam @NotNull TaskStatusType status) {
		
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskIds, usid))
				.then(etagHelper.checkEtag(et -> subtaskService.sumVersionByIds(subtaskIds).map(s -> s == et)))
				.filter(Boolean::booleanValue)
				.thenEmpty(taskStatusService.updateSubtaskStatus(status, subtaskIds));
	}
	
	@FindSubtasksApiDoc
	@GetMapping(params = {"taskId"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Map<String, Object>> findByTask(ETagHelper etagHelper,
			@RequestParam @Min(1) int taskId, Pageable pageable,
			@RequestParam(name = "contains", required = false) @Size(max = 125) String contains,
			@RequestParam(name = "status", required = false) TaskStatusType status,
			@RequestParam(name = "fields", required = false) String... fields) {
		
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(taskId, usid))
				.then(etagHelper.checkEtag(et -> subtaskService.sumVersionByTask(taskId, pageable, contains, status).map(s -> s == et)))
				.filter(c -> !c)
				.flatMapMany(c -> subtaskService.findByTask(taskId, pageable, contains, status, fields))
				.as(etagHelper::setETagWithMap);
	}
	
	@FindSubtaskApiDoc
	@GetMapping(path = "/{subtaskId}", produces = MediaType.APPLICATION_JSON_VALUE)	
	public Mono<SubtaskDTO> findById(ETagHelper etagHelper, @PathVariable @Min(1) int subtaskId) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskId, usid))
				.then(etagHelper.checkEtag(et -> subtaskService.existsByIdAndVersion(subtaskId, et)))
				.filter(c -> !c)
				.flatMap(c -> subtaskService.findById(subtaskId))
				.as(etagHelper::setEtag);
	}
	
	@CountSubtasksApiDoc
	@GetMapping(path = "/count/{taskId}")
	public Mono<CountDTO> countByTask(@PathVariable @Min(1) int taskId) {
		return subtaskService.countByTask(taskId);
	}
	
}
