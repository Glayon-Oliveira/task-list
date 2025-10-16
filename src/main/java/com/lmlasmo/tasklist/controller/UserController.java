package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.controller.util.ETagCheck;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.update.UpdatePasswordDTO;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

	private UserService userService;	
	
	@PutMapping("/i")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Void updatePassword(@RequestBody @Valid UpdatePasswordDTO update, HttpServletRequest req, HttpServletResponse res) {
		int id = AuthenticatedTool.getUserId();
		
		ETagCheck.check(req, res, et -> userService.existsByIdAndVersion(id, et));
		
		userService.updatePassword(AuthenticatedTool.getUserId(), update.getPassword());
		return null;
	}
	
	@DeleteMapping("/i")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Void delete() {		
		userService.delete(AuthenticatedTool.getUserId());
		return null;
	}
		
	@GetMapping("/i")	
	public UserDTO findByI(HttpServletRequest req, HttpServletResponse res) {
		int id = AuthenticatedTool.getUserId();
		
		if(ETagCheck.check(req, res, et -> userService.existsByIdAndVersion(id, et))) return null;
		
		return userService.findById(id);
	}
	
}
