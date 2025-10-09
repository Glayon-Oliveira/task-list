package com.lmlasmo.tasklist.dto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDTO {
	
	@JsonProperty	
	private int id;
	
	@JsonProperty	
	private String name;	
	
	@JsonProperty
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
	
	@JsonIgnore
	private long version;
	
	@JsonProperty
	@JsonInclude(content = Include.NON_NULL)
	private Set<SubtaskDTO> subtasks;
	
	public TaskDTO(Task task) {
		this.id = task.getId();
		this.name = task.getName();
		this.summary = task.getSummary();
		this.createdAt = task.getCreatedAt();
		this.updatedAt = task.getUpdatedAt();
		this.status = task.getStatus();
		this.version = task.getVersion();
		this.deadlineZone = task.getDeadlineZone();
		this.deadline = task.getDeadline().atZone(ZoneId.of(deadlineZone)).toOffsetDateTime();		
	}
	
	public TaskDTO(Task task, boolean withSubtasks) {
		this(task);
		
		if(withSubtasks) this.subtasks = task.getSubtasks().stream()
				.map(SubtaskDTO::new)
				.collect(Collectors.toSet());
	}
	
	public void setDeadline(OffsetDateTime deadline) {
		this.deadline = deadline.withSecond(0).withNano(0);
	}

}
