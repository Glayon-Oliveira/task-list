package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.doc.controller.account.LinkEmailApiDoc;
import com.lmlasmo.tasklist.doc.controller.account.PrimaryEmailApiDoc;
import com.lmlasmo.tasklist.doc.controller.account.EmailStatusApiDoc;
import com.lmlasmo.tasklist.dto.auth.EmailDTO;
import com.lmlasmo.tasklist.dto.auth.EmailWithConfirmationDTO;
import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.security.AuthenticatedResourceAccess;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;
import com.lmlasmo.tasklist.service.UserEmailService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/api/account")
@Validated
public class AccountController {
	
	private UserEmailService userEmailService;
	private EmailConfirmationService confirmationService;
	private	AuthenticatedResourceAccess resourceAccess;

	@LinkEmailApiDoc
	@PostMapping("/email/link")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> linkEmail(@RequestBody @Valid EmailWithConfirmationDTO email) {
		return AuthenticatedTool.getUserId()
				.flatMap(usid -> {
					return confirmationService.valideCodeHash(email.getConfirmation(), email.getEmail(), EmailConfirmationScope.LINK)
							.then(userEmailService.save(email.getEmail(), usid));
				}).then();
	}
	
	@PrimaryEmailApiDoc
	@PatchMapping("/email/primary/{id}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> changePrimaryEmail(@PathVariable @Min(1) int id) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessEmail(id, usid))
				.flatMap(usid -> userEmailService.changePrimaryEmail(id, usid))
				.then();
	}
	
	@EmailStatusApiDoc
	@PatchMapping("/email/status/{status}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('ADMIN')")
	public Mono<Void> changeStatusEmail(@RequestBody @Valid EmailDTO email, @PathVariable @NotNull EmailStatusType status) {
		return userEmailService.changeEmailStatus(email.getEmail(), status)
				.then();
	}
	
	@EmailStatusApiDoc
	@PostMapping("/email/terminate")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> terminate(@RequestBody @Valid EmailDTO email) {
		return resourceAccess.canAccess((usid, can) -> can.canAccessEmail(email.getEmail(), usid))
				.then(userEmailService.changeEmailStatus(email.getEmail(), EmailStatusType.SUSPENDED))
				.then();
	}
	
}
