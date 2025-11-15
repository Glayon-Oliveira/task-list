package com.lmlasmo.tasklist.controller;

import java.util.List;

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
import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	@PreAuthorize("@resourceAccessService.canAccessTask(#create.taskId, @authTool.getUserId())")
	public SubtaskDTO create(@RequestBody @Valid CreateSubtaskDTO create) {
		return subtaskService.save(create).block();
	}
	
	@DeleteMapping(params = "subtaskIds")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessSubtask(#subtaskIds, @authTool.getUserId())")
	public Void delete(@RequestParam List<@Min(1) Integer> subtaskIds, HttpServletRequest req, HttpServletResponse res) {
		ETagCheck.check(req, res, et -> subtaskService.sumVersionByIds(subtaskIds).block().equals(et));
		subtaskService.delete(subtaskIds).block();
		return null;
	}
	
	@PatchMapping("/{subtaskId}")
	@PreAuthorize("@resourceAccessService.canAccessSubtask(#subtaskId, @authTool.getUserId())")
	public SubtaskDTO update(@PathVariable @Min(1) int subtaskId, @RequestBody UpdateSubtaskDTO update, 
			HttpServletRequest req, HttpServletResponse res) {
		
		ETagCheck.check(req, res, et -> subtaskService.existsByIdAndVersion(subtaskId, et).block());
		
		return subtaskService.update(subtaskId, update).block();
	}
	
	@PatchMapping(path = "/{subtaskId}", params = "position")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessSubtask(#subtaskId, @authTool.getUserId())")
	public Void updateSubtaskPosition(@PathVariable @Min(1) int subtaskId, @RequestParam @Min(1) int position,
			HttpServletRequest req, HttpServletResponse res) {
		
		ETagCheck.check(req, res, et -> subtaskService.existsByIdAndVersion(subtaskId, et).block());
		
		subtaskService.updatePosition(subtaskId, position).block();
		return null;
	}
	
	@PatchMapping(params = {"subtaskIds", "status"})
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccessService.canAccessSubtask(#subtaskIds, @authTool.getUserId())")
	public Void updateSubtaskStatus(@RequestParam List<@Min(1) Integer> subtaskIds, @RequestParam @NotNull TaskStatusType status, 
			HttpServletRequest req, HttpServletResponse res) {
		
		ETagCheck.check(req, res, et -> subtaskService.sumVersionByIds(subtaskIds).block().equals(et));
		
		taskStatusService.updateSubtaskStatus(status, subtaskIds).block();
		return null;
	}

	@GetMapping(params = {"taskId"})
	@PreAuthorize("@resourceAccessService.canAccessTask(#taskId, @authTool.getUserId())")
	public List<SubtaskDTO> findByTask(@RequestParam @Min(1) int taskId, HttpServletRequest req, HttpServletResponse res) {
		if(ETagCheck.check(req, res, et -> subtaskService.sumVersionByTask(taskId).block().equals(et))) return null;
		
		return subtaskService.findByTask(taskId).collectList().block();
	}
	
	@GetMapping("/{subtaskId}")
	@PreAuthorize("@resourceAccessService.canAccessSubtask(#subtaskId, @authTool.getUserId())")
	public SubtaskDTO findById(@PathVariable @Min(1) int subtaskId, HttpServletRequest req, HttpServletResponse res) {
		if(ETagCheck.check(req, res, et -> subtaskService.existsByIdAndVersion(subtaskId, et).block())) return null;
		
		return subtaskService.findById(subtaskId).block();
	}
	
}
