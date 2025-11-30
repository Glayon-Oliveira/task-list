package com.lmlasmo.tasklist.doc.controller.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lmlasmo.tasklist.doc.SimpleApiDoc;
import com.lmlasmo.tasklist.doc.StatusResponseApiDoc;

import io.swagger.v3.oas.annotations.tags.Tag;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SimpleApiDoc(
		summary = "Requests email confirmation",
		description = """
					It requests a confirmation code to be sent via email and returns a hash and a timestamp,
					which must be provided later along with the code for validation.
				    
					When the scope is **LINK**, the email cannot be in use in the system.
				""",
		success = @StatusResponseApiDoc(status = 200, message = "Email requested successfully"),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data"),
				@StatusResponseApiDoc(status = 409, message = "Email already in use")
			}
		)
@Tag(name = "auth-controller")
public @interface EmailConfirmationApiDoc {}
