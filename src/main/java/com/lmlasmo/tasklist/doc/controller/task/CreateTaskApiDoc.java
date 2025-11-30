package com.lmlasmo.tasklist.doc.controller.task;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lmlasmo.tasklist.doc.SimpleApiDoc;
import com.lmlasmo.tasklist.doc.StatusResponseApiDoc;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SecurityRequirement(name = "bearerAuth")
@SimpleApiDoc(
		summary = "Create a task",
		description = """
					Creates a new task linked to the authenticated user.

				    Fields of focus:

				    - **deadline**: Task expiration date. It can be null if there is no deadline.
				    - **deadlineZone**: Deadline time zone. Must be compatible with ZoneId.
					""",
		success = @StatusResponseApiDoc(status = 200, message = "Task created successfully"),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data")
			}
		)
@Tag(name = "task-controller")
public @interface CreateTaskApiDoc {}
