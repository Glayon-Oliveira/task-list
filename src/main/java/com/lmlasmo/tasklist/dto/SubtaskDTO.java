package com.lmlasmo.tasklist.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubtaskDTO implements VersionedDTO {
	
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
	private BigDecimal position;
	
	@JsonProperty
	private Instant createdAt;
	
	@JsonProperty
	private Instant updatedAt;
	
	@JsonProperty
	private long version;

}
