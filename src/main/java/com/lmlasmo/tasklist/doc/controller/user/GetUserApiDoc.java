package com.lmlasmo.tasklist.doc.controller.user;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lmlasmo.tasklist.doc.ETagSupport;
import com.lmlasmo.tasklist.doc.ETagSupport.ETagType;
import com.lmlasmo.tasklist.doc.SimpleApiDoc;
import com.lmlasmo.tasklist.doc.StatusResponseApiDoc;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@SecurityRequirement(name = "bearerAuth")
@ETagSupport(type = ETagType.IF_NONE_MATCH)
@SimpleApiDoc(
		summary = "Find authenticated user",
		description = """
				   Returns the information of the currently authenticated user.
				   """,
	    success = {
	    		@StatusResponseApiDoc(status = 200, message = "User data successfully returned")
	    	}
		)
@Tag(name = "user-controller")
public @interface GetUserApiDoc {}
