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
		summary = "Updates the status of a subtask.",
		description = """
					Updates the status of one or more subtasks.
				
					The status of the main task will be automatically adjusted based
					on the combined statuses of all subtasks.
					""",
		success = @StatusResponseApiDoc(status = 204, message = "Subtask status updated successfully"),		
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data"),
				@StatusResponseApiDoc(status = 404, message = "Subtask not found"),
				@StatusResponseApiDoc(status = 409, message = "The subtasks were modified or removed during the process"),
			}
		)
@Tag(name = "subtask-controller")
public @interface UpdateSubtaskStatusApiDoc {}
