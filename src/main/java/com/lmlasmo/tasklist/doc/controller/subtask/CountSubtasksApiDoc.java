package com.lmlasmo.tasklist.doc.controller.subtask;

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
		summary = "Count subtasks by target task",
		description = """
					Returns the total number of subtasks.
					""",
		success = @StatusResponseApiDoc(status = 200, message = "Successfully"),
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Task id value is invalid"),
				@StatusResponseApiDoc(status = 404, message = "Task not found")
			}
		)
@Tag(name = "subtask-controller")
public @interface CountSubtasksApiDoc {

}
