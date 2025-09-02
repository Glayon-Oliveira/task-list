package com.lmlasmo.tasklist.dto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskDTO {
	
	@JsonProperty	
	private int id;
	
	@JsonProperty	
	private String name;
	
	@JsonProperty(required = false)
	private String summary;
	
	@JsonProperty		
	private OffsetDateTime deadline;
	
	@JsonProperty	
	private String deadlineZone;
	
	@JsonProperty	
	private Instant createdAt;
	
	@JsonProperty	
	private Instant updatedAt;
	
	@JsonProperty	
	private TaskStatusType status;
	
	@JsonProperty	
	private int userId;
	
	public TaskDTO(Task task) {
		this.id = task.getId();
		this.name = task.getName();
		this.summary = task.getSummary();		
		this.createdAt = task.getCreatedAt();
		this.updatedAt = task.getUpdatedAt();
		this.status = task.getStatus();
		this.userId = task.getUser().getId();
		this.deadline = task.getDeadline().atOffset(ZoneOffset.UTC).withSecond(0).withNano(0);
		this.deadlineZone = task.getDeadlineZone();
	}

}
