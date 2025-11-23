package com.lmlasmo.tasklist.controller;

import java.util.List;

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
import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskPositionDTO;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.security.AuthenticatedResourceAccess;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
	
	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<SubtaskDTO> create(@RequestBody @Valid CreateSubtaskDTO create) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(create.getTaskId(), usid))
				.then(subtaskService.save(create));
	}
	
	@DeleteMapping(params = "subtaskIds")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> delete(@RequestParam List<@Min(1) Integer> subtaskIds) {		
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskIds, usid))
				.then(ETagHelper.checkEtag(et -> subtaskService.sumVersionByIds(subtaskIds).map(et::equals)))
				.filter(Boolean::booleanValue)
				.thenEmpty(subtaskService.delete(subtaskIds));
	}
	
	@PatchMapping("/{subtaskId}")
	public Mono<SubtaskDTO> update(@PathVariable @Min(1) int subtaskId, @RequestBody @Valid UpdateSubtaskDTO update) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskId, usid))
				.then(ETagHelper.checkEtag(et -> subtaskService.existsByIdAndVersion(subtaskId, et)))
				.filter(Boolean::booleanValue)
				.then(subtaskService.update(subtaskId, update));
	}
	
	@PatchMapping(path = "/{subtaskId}/position")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> updateSubtaskPosition(@PathVariable @Min(1) int subtaskId, @RequestBody @Valid UpdateSubtaskPositionDTO update) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskId, usid))
				.then(resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(update.getAnchorSubtaskId(), usid)))
				.then(ETagHelper.checkEtag(et -> subtaskService.existsByIdAndVersion(subtaskId, et)))
				.filter(Boolean::booleanValue)
				.thenEmpty(subtaskService.updatePosition(subtaskId, update));
	}
	
	@PatchMapping(params = {"subtaskIds", "status"})
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> updateSubtaskStatus(@RequestParam List<@Min(1) Integer> subtaskIds, @RequestParam @NotNull TaskStatusType status) {		
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskIds, usid))
				.then(ETagHelper.checkEtag(et -> subtaskService.sumVersionByIds(subtaskIds).map(et::equals)))
				.filter(Boolean::booleanValue)
				.thenEmpty(taskStatusService.updateSubtaskStatus(status, subtaskIds));
	}

	@GetMapping(params = {"taskId"})
	public Flux<SubtaskDTO> findByTask(@RequestParam @Min(1) int taskId) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessTask(taskId, usid))
				.then(ETagHelper.checkEtag(et -> subtaskService.sumVersionByTask(taskId).map(et::equals)))
				.filter(c -> !c)
				.thenMany(subtaskService.findByTask(taskId))
				.as(ETagHelper::setEtag);
	}
	
	@GetMapping("/{subtaskId}")	
	public Mono<SubtaskDTO> findById(@PathVariable @Min(1) int subtaskId) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessSubtask(subtaskId, usid))
				.then(ETagHelper.checkEtag(et -> subtaskService.existsByIdAndVersion(subtaskId, et)))
				.filter(Boolean::booleanValue)
				.flatMap(c -> subtaskService.findById(subtaskId))
				.as(ETagHelper::setEtag);
	}
	
}
