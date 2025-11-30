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
		summary = "Updates the position of a subtask",
		description = """
					Updates the position of a subtask based on an anchor subtask and a direction 
					(BEFORE or AFTER), defining whether it will be immediately before or after the anchor.
				    
				    If the system detects a conflict or risk of overflow in the sorting, an automatic
				    normalization process will be executed to recalculate and stabilize the positions,
				    ensuring the consistency of the list.
					""",
		success = @StatusResponseApiDoc(status = 204, message = "Subtask status updated successfully"),		
		errors = {
				@StatusResponseApiDoc(status = 400, message = "Invalid or missing data"),
				@StatusResponseApiDoc(status = 404, message = "Subtask not found"),
				@StatusResponseApiDoc(status = 409, message = "The subtasks were modified or removed during the process"),
			}
		)
@Tag(name = "subtask-controller")
public @interface UpdateSubtaskPositionApiDoc {}
