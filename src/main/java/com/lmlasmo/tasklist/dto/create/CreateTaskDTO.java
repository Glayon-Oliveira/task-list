package com.lmlasmo.tasklist.dto.create;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.validation.ValidZoneId;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateTaskDTO {
	
	@JsonProperty
	@NotEmpty
	private String name;
	
	@JsonProperty(required = false)
	private String summary;
	
	@JsonProperty(required = false)
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
