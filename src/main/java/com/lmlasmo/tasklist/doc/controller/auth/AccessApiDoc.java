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
		summary = "Create a new access token.",
		description = """
					Generates a new Access Token from a valid Refresh Token provided by the client.
					
					The client can provide the refresh token either:
                    - In the request body.
                    - Via a request cookie.
					
				    The Access Token is always returned in the response body.
					""",
		success = @StatusResponseApiDoc(status = 200, message = "Successful creation of the access token"),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data, or invalid refresh token")
			}
		)
@Tag(name = "auth-controller")
public @interface AccessApiDoc {}
