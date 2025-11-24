package com.lmlasmo.tasklist.dto.create;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSubtaskDTO {	
	
	@JsonProperty
	@NotBlank
	private String name;
	
	@JsonProperty(required = false)
	private String summary;
	
	@JsonProperty(required = false)
	@Min(0)
	private int durationMinutes = 5;
	
	@JsonProperty
	@Min(1)
	private int taskId;
	
}
