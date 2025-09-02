package com.lmlasmo.tasklist.controller;

import java.util.List;

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

import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/subtask")
public class SubtaskController {
	
	private SubtaskService subtaskService;
	private TaskStatusService taskStatusService;
	
	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	@PreAuthorize("@resourceAccessService.canAccessTask(#create.taskId, authentication.principal)")
	public SubtaskDTO create(@RequestBody @Valid CreateSubtaskDTO create) {
		return subtaskService.save(create);
	}
	
	@DeleteMapping(params = "subtaskIds")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessSubtask(#subtaskIds, authentication.principal)")
	public Void delete(@RequestParam List<@Min(1) Integer> subtaskIds) {
		subtaskService.delete(subtaskIds);
		return null;
	}
	
	@PutMapping(path = "/{subtaskId}", params = "position")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessSubtask(#subtaskId, authentication.principal)")
	public Void updateSubtaskPosition(@PathVariable @Min(1) int subtaskId, @RequestParam @Min(1) int position) {
		subtaskService.updatePosition(subtaskId, position);
		return null;
	}
	
	@PutMapping(params = {"subtaskIds", "status"})
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessSubtask(#subtaskIds, authentication.principal)")
	public Void updateSubtaskStatus(@RequestParam List<@Min(1) Integer> subtaskIds, @RequestParam @NotNull TaskStatusType status) {
		taskStatusService.updateSubtaskStatus(status, subtaskIds);
		return null;
	}

	@GetMapping(params = "taskId")
	@PreAuthorize("@resourceAccessService.canAccessTask(#taskId, authentication.principal)")
	public Page<SubtaskDTO> findByTask(@RequestParam @Min(1) int taskId, Pageable pageable){
		return subtaskService.findByTask(taskId, pageable);
	}
	
}
