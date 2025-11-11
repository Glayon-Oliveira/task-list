package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.controller.util.ETagHelper;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.service.UserService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

	private UserService userService;
	
	@DeleteMapping("/i")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> delete() {
		return AuthenticatedTool.getUserId()
				.flatMap(usid -> {
					return ETagHelper.checkEtag(et -> userService.existsByIdAndVersion(usid, et))
							.filter(Boolean::valueOf)
							.flatMap(c -> userService.delete(usid));
				});
	}
		
	@GetMapping("/i")	
	public Mono<UserDTO> findByI() {
		return AuthenticatedTool.getUserId()
				.flatMap(usid -> {
					return ETagHelper.checkEtag(et -> userService.existsByIdAndVersion(usid, et))
							.filter(Boolean::booleanValue)
							.flatMap(c -> userService.findById(usid))
							.as(ETagHelper::setEtag);
				});
	}
	
}
