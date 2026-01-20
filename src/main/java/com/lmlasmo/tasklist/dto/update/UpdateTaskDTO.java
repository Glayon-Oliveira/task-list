package com.lmlasmo.tasklist.dto.update;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.validation.ValidZoneId;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskDTO {

	@JsonProperty(required = false)
	@Size(max = 128)
	@Pattern(regexp = ".*\\S.*", message = "Not can blank")
	private String name;
	
	@JsonProperty(required = false)
	@Size(max = 4096)
	@Pattern(regexp = ".*\\S.*", message = "Not can blank")
	private String summary;
	
	@JsonProperty(required = false)
	@Future
	private OffsetDateTime deadline;
	
	@JsonProperty(required = false)
	@ValidZoneId
	private String deadlineZone;
		
	public void setDeadline(OffsetDateTime deadline) {
		if(deadline != null) this.deadline = deadline.withNano(0).withSecond(0);		
	}
}
