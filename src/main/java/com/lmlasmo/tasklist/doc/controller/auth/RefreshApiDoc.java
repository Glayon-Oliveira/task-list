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
		summary = "Create a new refresh token.",
		description = """
					Generates a new Refresh Token from a valid token provided by the client.
					
					The token can be sent in the body or obtained automatically from the cookie, which has priority.
				    The new Refresh Token is also set in the response cookies.
					""",
		success = @StatusResponseApiDoc(status = 200, message = "Refresh Token successfully renewed"),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data, or invalid refresh token"),
			}
		)
@Tag(name = "auth-controller")
public @interface RefreshApiDoc {}
