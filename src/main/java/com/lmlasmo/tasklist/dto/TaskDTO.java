package com.lmlasmo.tasklist.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskDTO {
	
	@JsonProperty(required = false)
	@Null
	private int id;
	
	@JsonProperty
	@NotEmpty
	private String name;
	
	@JsonProperty
	@NotEmpty
	private String task;
	
	@JsonProperty(required = false)
	@Null
	private LocalDateTime timestamp;
	
	@JsonProperty(required = false)
	@Null
	private TaskStatusType status;
	
	@JsonProperty(value = "user_id")
	@Min(1)
	private int userId;
	
	public TaskDTO(Task task) {
		this.id = task.getId();
		this.name = task.getName();
		this.timestamp = task.getTimestamp();
		this.status = task.getStatus();
		this.userId = task.getUser().getId();
	}

}
