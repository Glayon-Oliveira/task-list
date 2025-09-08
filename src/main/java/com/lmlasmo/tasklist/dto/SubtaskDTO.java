package com.lmlasmo.tasklist.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubtaskDTO {
	
	@JsonProperty	
	private int id;
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	private String summary;
	
	@JsonProperty
	private int durationMinutes;
	
	@JsonProperty
	private TaskStatusType status;
	
	@JsonProperty	
	private Integer position;
	
	@JsonProperty
	private Instant createdAt;
	
	@JsonProperty
	private Instant updatedAt;
	
	@JsonProperty	
	private int taskId;
	
	public SubtaskDTO(Subtask subtask) {
		this.id = subtask.getId();
		this.name = subtask.getName();
		this.summary = subtask.getSummary();
		this.durationMinutes = subtask.getDurationMinutes();
		this.status = subtask.getStatus();
		this.position = subtask.getPosition();
		this.createdAt = subtask.getCreatedAt();
		this.updatedAt = subtask.getUpdatedAt();
		this.taskId = subtask.getTask().getId();
	}

}
