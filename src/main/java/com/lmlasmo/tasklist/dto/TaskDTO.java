package com.lmlasmo.tasklist.dto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.validation.ValidZoneId;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskDTO {
	
	@JsonProperty(required = false)	
	private int id;
	
	@JsonProperty
	@NotEmpty
	private String name;
	
	@JsonProperty(required = false)
	private String summary;
	
	@JsonProperty	
	@NotNull
	@Future
	private OffsetDateTime deadline;
	
	@JsonProperty
	@NotBlank
	@ValidZoneId
	private String deadlineZone;
	
	@JsonProperty(required = false)
	@Null
	private Instant createdAt;
	
	@JsonProperty(required = false)
	@Null
	private Instant updatedAt;
	
	@JsonProperty(required = false)
	@Null
	private TaskStatusType status;
	
	@JsonProperty(value = "user_id")
	@Min(1)
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
