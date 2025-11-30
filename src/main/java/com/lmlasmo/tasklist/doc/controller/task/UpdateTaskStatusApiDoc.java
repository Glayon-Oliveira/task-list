package com.lmlasmo.tasklist.doc.controller.task;

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
		summary = "Update task status",
		description = """
				 	Update the status of a specific task.

				    If a task has subtasks, the status will be automatically determined based
				    on their state; manual status setting is not allowed.
					""",
		success = @StatusResponseApiDoc(status = 204, message = "Task status updated successfully"),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data, or an attempt to update the status of a task with subtasks"),
				@StatusResponseApiDoc(status = 404, message = "Task not found"),
				@StatusResponseApiDoc(status = 409, message = "The task was modified or removed during the process"),
			}
		)
@Tag(name = "task-controller")
public @interface UpdateTaskStatusApiDoc {}
