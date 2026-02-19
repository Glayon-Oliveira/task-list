package com.lmlasmo.tasklist.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.doc.controller.user.DeleteUserApiDoc;
import com.lmlasmo.tasklist.doc.controller.user.GetUserApiDoc;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.service.UserService;
import com.lmlasmo.tasklist.web.util.ETagHelper;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

	private UserService userService;
	
	@DeleteUserApiDoc
	@DeleteMapping("/i")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> delete(ETagHelper etagHelper) {
		return AuthenticatedTool.getUserId()
				.flatMap(usid -> {
					return etagHelper.checkEtag(et -> userService.existsByIdAndVersion(usid, et))
							.filter(Boolean::valueOf)
							.flatMap(c -> userService.delete(usid));
				});
	}
	
	@GetUserApiDoc
	@GetMapping(path = "/i", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<UserDTO> findByI(ETagHelper etagHelper) {
		return AuthenticatedTool.getUserId()
				.flatMap(usid -> {
					return etagHelper.checkEtag(et -> userService.existsByIdAndVersion(usid, et))
							.filter(c -> !c)
							.flatMap(c -> userService.findById(usid))
							.as(etagHelper::setEtag);
				});
	}
	
}
