package com.lmlasmo.tasklist.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubtaskDTO {
	
	@JsonProperty(required = false)	
	private int id;
	
	@JsonProperty
	@NotBlank
	private String name;
	
	@JsonProperty(required = false)
	private String summary;
	
	@JsonProperty(required = false)
	@Min(1)
	private int durationMinutes = 5;
	
	@JsonProperty(required = false)
	@Null
	private TaskStatusType status;
	
	@JsonProperty
	@Null
	private Integer position;
	
	@JsonProperty(required = false)
	private Instant createdAt;
	
	@JsonProperty(required = false)
	private Instant updatedAt;
	
	@JsonProperty
	@Min(1)
	private int taskId;
	
	public SubtaskDTO(Subtask subtask) {
		this.id = subtask.getId();
		this.name = subtask.getName();
		this.durationMinutes = subtask.getDurationMinutes();
		this.status = subtask.getStatus();
		this.position = subtask.getPosition();
		this.createdAt = subtask.getCreatedAt();
		this.updatedAt = subtask.getUpdatedAt();
		this.taskId = subtask.getTask().getId();
	}

}
