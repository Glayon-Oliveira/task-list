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
		summary = "Log in",
		description = """
					Authenticates the user in the system.
				
					If the client provides the header 'X-RefreshToken-Body-Provide', 
                    the Refresh Token is returned in the response body. 
                    Otherwise, it is set in a response cookie.

                    The Access Token is always returned in the response body.  
					""",
		success = @StatusResponseApiDoc(status = 200, message = "Login successful"),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data"),
				@StatusResponseApiDoc(status = 401, message = "Authentication failed due to invalid credentials or user status")
			}
		)
@Tag(name = "auth-controller")
public @interface LoginApiDoc {}
