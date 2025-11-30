package com.lmlasmo.tasklist.doc.controller.user;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lmlasmo.tasklist.doc.ETagSupport;
import com.lmlasmo.tasklist.doc.SimpleApiDoc;
import com.lmlasmo.tasklist.doc.StatusResponseApiDoc;
import com.lmlasmo.tasklist.doc.ETagSupport.ETagType;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@SecurityRequirement(name = "bearerAuth")
@ETagSupport(type = ETagType.IF_MATCH)
@SimpleApiDoc(
		summary = "Delete user",
	    description = """
	            	Permanently deletes the currently authenticated user.
	            	""",
	    success = {
	    		@StatusResponseApiDoc(status = 204, message = "User successfully removed"),	    		
	    	},
	    errors = {
	            @StatusResponseApiDoc(status = 409, message = "The user was updated or removed during the process")
	    	}
		)
@Tag(name = "user-controller")
public @interface DeleteUserApiDoc {}
