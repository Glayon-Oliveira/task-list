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
		summary = "Find all subtasks",
		description = """
					Pagination supports:
					
					page: Defines the offset of the search. Default value is 0.
					size: Defines the maximum number of tasks returned by page. Default value is 50.
					sort: Defines the sorting based on a field. The format is "field,ASC/DESC".
					
					The following additional parameters can be used:
					
					status: Filters tasks by status.
					contains: Specifies a string to search for in the name and summary fields.
					fields: Filters which fields are included in the response. If empty, all fields
						are returned. Some fields are automatically included in the response.
					
					Returns all subtasks associated with a task.
					""",
		success = @StatusResponseApiDoc(status = 200, message = "List of subtasks returned successfully"),
		errors = {
				@StatusResponseApiDoc(status = 404, message = "Subtask not found"),
			}
		)
@Tag(name = "task-controller")
public @interface FindSubtasksApiDoc {}
