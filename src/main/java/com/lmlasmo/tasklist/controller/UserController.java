package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.service.UserService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
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
	
}
