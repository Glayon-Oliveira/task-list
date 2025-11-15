package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.controller.util.ETagCheck;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.security.AuthenticatedTool.DirectAuthenticatedTool;
import com.lmlasmo.tasklist.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

	private UserService userService;	
	
	@DeleteMapping("/i")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Void delete(HttpServletRequest req, HttpServletResponse res) {
		int id = DirectAuthenticatedTool.getUserId();
				
		ETagCheck.check(req, res, et -> userService.existsByIdAndVersion(id, et).block());		
		userService.delete(id).block();
		
		return null;
	}
		
	@GetMapping("/i")	
	public UserDTO findByI(HttpServletRequest req, HttpServletResponse res) {
		int id = DirectAuthenticatedTool.getUserId();
		
		if(ETagCheck.check(req, res, et -> userService.existsByIdAndVersion(id, et).block())) return null;
		
		return userService.findById(id).block();
	}
	
}
