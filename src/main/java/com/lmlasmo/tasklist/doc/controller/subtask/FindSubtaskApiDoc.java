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
@ETagSupport(type = ETagType.IF_NONE_MATCH)
@SimpleApiDoc(
		summary = "Find a subtask",
		description = """
				Returns a subtask from the user.
				""",
		success = @StatusResponseApiDoc(status = 200, message = "Subtask returned successfully"),
		errors = {
				@StatusResponseApiDoc(status = 404, message = "Subtask not found")
			}
		)
@Tag(name = "task-controller")
public @interface FindSubtaskApiDoc {}
