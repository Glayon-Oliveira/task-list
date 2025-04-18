package com.lmlasml.tasklist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasml.tasklist.dto.TaskDTO;
import com.lmlasml.tasklist.dto.TaskStatusDTO;
import com.lmlasml.tasklist.service.TaskService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/task")
public class TaskController {

	private TaskService service;
	
	@PostMapping("/")
	@ResponseBody
	public TaskDTO create(@RequestBody @Valid TaskDTO create) {
		create = service.save(create);
		return create;
	}	
	
	@PutMapping("/")
	@ResponseBody
	public TaskDTO updateStatus(@RequestBody @Valid TaskStatusDTO status) {
		return service.update(status);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> delete(@PathVariable int id) {
		service.delete(id);
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/")	
	public Page<TaskDTO> findAllByI(Pageable pageable){
		int id = (int) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return service.findByUser(id, pageable);
	}
	
}
