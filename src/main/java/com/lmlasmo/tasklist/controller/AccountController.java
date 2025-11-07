package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.dto.auth.EmailDTO;
import com.lmlasmo.tasklist.dto.auth.EmailWithConfirmationDTO;
import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.security.AuthenticatedTool.DirectAuthenticatedTool;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;
import com.lmlasmo.tasklist.service.UserEmailService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountController {
	
	private UserEmailService userEmailService;
	private EmailConfirmationService confirmationService;	

	@PostMapping("/email/link")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Void linkEmail(@RequestBody @Valid EmailWithConfirmationDTO email) {
		int id = DirectAuthenticatedTool.getUserId();
		
		confirmationService.valideCodeHash(email.getConfirmation(), EmailConfirmationScope.LINK);
		
		userEmailService.save(email.getEmail(), id);
		return null;
	}
	
	@PatchMapping("/email/primary/{id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccess.canAccessEmail(#id, @authTool.getUserId())")
	public Void changePrimaryEmail(@PathVariable int id) {
		userEmailService.changePrimaryEmail(id, DirectAuthenticatedTool.getUserId());
		return null;
	}
	
	@PatchMapping("/email/status/{status}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public Void changeStatusEmail(@RequestBody EmailDTO email, @PathVariable EmailStatusType status) {
		userEmailService.changeEmailStatus(email.getEmail(), status);
		return null;
	}
		
	@PostMapping("/email/terminate")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("@resourceAccess.canAccessEmail(#email.email, @authTool.getUserId())")
	public Void terminate(@RequestBody @Valid EmailDTO email) {		
		userEmailService.changeEmailStatus(email.getEmail(), EmailStatusType.SUSPENDED);
		return null;
	}
	
}
