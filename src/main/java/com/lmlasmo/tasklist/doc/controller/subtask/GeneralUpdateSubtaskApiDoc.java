package com.lmlasmo.tasklist.doc.controller.subtask;

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

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SecurityRequirement(name = "bearerAuth")
@ETagSupport(type = ETagType.IF_MATCH)
@SimpleApiDoc(
		summary = "Update the data for a subtask",
		description = """
				Updates the main data of a subtask, except for the status and position.
				""",
		success = @StatusResponseApiDoc(status = 200, message = "Subtask updated successfully"),		
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data"),
				@StatusResponseApiDoc(status = 404, message = "Subtask not found"),
				@StatusResponseApiDoc(status = 409, message = "The subtask was modified or removed during the process"),
			}
		)
@Tag(name = "subtask-controller")
public @interface GeneralUpdateSubtaskApiDoc {}
