package com.lmlasmo.tasklist.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.service.UserService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

	private UserService userService;	
	
	@PutMapping("/")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public UserDTO updatePassword(@RequestParam String password) {		
		return userService.updatePassword(AuthenticatedTool.getUserId(), password);		
	}
	
	@DeleteMapping("/")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public Void delete() {		
		userService.delete(AuthenticatedTool.getUserId());
		return null;
	}
		
	@GetMapping("/i")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public UserDTO findByI() {				
		return userService.findById(AuthenticatedTool.getUserId());		
	}
	
	@GetMapping("/")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)	
	public Page<UserDTO> findAll(Pageable pageable){
		return userService.findAll(pageable);
	}
	
}
