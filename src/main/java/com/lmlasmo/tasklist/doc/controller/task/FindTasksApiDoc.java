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
@ETagSupport(type = ETagType.IF_NONE_MATCH)
@SimpleApiDoc(
		summary = "Find all the tasks",
		description = """
					Pagination supports:
					
					page: Defines the offset of the search. Default value is 0.
					size: Defines the maximum number of tasks returned by page. Default value is 50.
					
					The following filter parameters can be used:
					
					status: Filters tasks by status.
					contains: Specifies a string to search for in the name and summary fields.
					
					Returns all tasks belonging to the authenticated user.
					""",
		success = @StatusResponseApiDoc(status = 200, message = "Task list returned successfully")
		)
@Tag(name = "task-controller")
public @interface FindTasksApiDoc {}
