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
		summary = "Count tasks",
		description = """
					Returns the total number of tasks.
					""",
		success = @StatusResponseApiDoc(status = 200, message = "Successfully")
		)
@Tag(name = "task-controller")
public @interface CountTasksApiDoc {

}
