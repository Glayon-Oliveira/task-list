package com.lmlasmo.tasklist.doc.controller.account;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lmlasmo.tasklist.doc.SimpleApiDoc;
import com.lmlasmo.tasklist.doc.StatusResponseApiDoc;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SecurityRequirement(name = "bearerAuth")
@SimpleApiDoc(
		summary = "Close an email",
		description = """
				Closes a user's email, assigning it the status *SUSPENDED*, preventing its use for login.
				""",
		success = @StatusResponseApiDoc(status = 204, message = "Email closed successfully."),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data"),
				@StatusResponseApiDoc(status = 404, message = "Email not found"),
				@StatusResponseApiDoc(status = 409, message = "The email status was modified during the process")
			}
		)
@Tag(name = "account-controller")
public @interface TerminateEmailApiDoc {}
