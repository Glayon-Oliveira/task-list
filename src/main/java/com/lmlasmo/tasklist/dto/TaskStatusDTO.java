package com.lmlasmo.tasklist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.TaskStatusType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskStatusDTO {
	
	@JsonProperty
	@NotNull
	@Min(1)
	private int id;
	
	@JsonProperty
	@NotNull
	private TaskStatusType status;	

}
