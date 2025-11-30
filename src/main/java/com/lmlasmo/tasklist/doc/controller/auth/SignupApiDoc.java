package com.lmlasmo.tasklist.doc.controller.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lmlasmo.tasklist.doc.SimpleApiDoc;
import com.lmlasmo.tasklist.doc.StatusResponseApiDoc;

import io.swagger.v3.oas.annotations.tags.Tag;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@SimpleApiDoc(
		summary = "Performs user registration",
		description = """
					Creates a new user in the system with a valid email address.
					
					The process requires a verification code accompanied by a hash and timestamp.
				    For more details on how this code works, see the endpoint related to
				    /api/auth/email/confirmation.
					""",
		success = @StatusResponseApiDoc(status = 201, message = "User created successfully."),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data"),
				@StatusResponseApiDoc(status = 409, message = "Username or email already in use")
			}
		)
@Tag(name = "auth-controller")
public @interface SignupApiDoc {}
