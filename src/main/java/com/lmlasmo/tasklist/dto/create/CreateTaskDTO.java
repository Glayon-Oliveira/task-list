package com.lmlasmo.tasklist.dto.create;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.validation.ValidZoneId;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateTaskDTO {
	
	@JsonProperty
	@NotBlank
	@Size(max = 128)
	private String name;
	
	@JsonProperty(required = false)
	@Size(max = 4096)
	private String summary;
	
	@JsonProperty(required = false)
	@NotNull
	@Future
	private OffsetDateTime deadline;
	
	@JsonProperty
	@NotBlank
	@ValidZoneId
	private String deadlineZone;
		
	public void setDeadline(OffsetDateTime deadline) {
		if(deadline != null) {
			this.deadline = deadline.withSecond(0).withNano(0);
		}		
	}
	
}
