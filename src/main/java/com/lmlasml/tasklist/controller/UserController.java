package com.lmlasml.tasklist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasml.tasklist.dto.UserDTO;
import com.lmlasml.tasklist.security.AuthenticatedTool;
import com.lmlasml.tasklist.service.UserService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

	private UserService userService;	
	
	@PutMapping("/")	
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Void updatePassword(@RequestParam String password) {		
		userService.updatePassword(AuthenticatedTool.getUserId(), password);
		return null;
	}
	
	@DeleteMapping("/")	
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Void delete() {		
		userService.delete(AuthenticatedTool.getUserId());
		return null;
	}
		
	@GetMapping("/i")	
	public UserDTO findByI() {				
		return userService.findById(AuthenticatedTool.getUserId());		
	}
	
	@GetMapping("/")
	public Page<UserDTO> findAll(Pageable pageable){
		return userService.findAll(pageable);
	}
	
}
