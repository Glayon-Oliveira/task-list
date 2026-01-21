package com.lmlasmo.tasklist.dto.create;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateSubtaskDTO {	
	
	@JsonProperty
	@NotBlank
	@Size(max = 128)
	private String name;
	
	@JsonProperty(required = false)
	@Size(max = 2048)
	private String summary;
	
	@JsonProperty(required = false)
	@Min(0)
	private int durationMinutes = 5;
	
	@JsonProperty
	@Min(1)
	private int taskId;
	
}
