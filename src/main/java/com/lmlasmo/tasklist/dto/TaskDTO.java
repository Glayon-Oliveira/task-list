package com.lmlasmo.tasklist.dto;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDTO implements VersionedDTO {
	
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
	
	@JsonProperty
	private long version;

}
