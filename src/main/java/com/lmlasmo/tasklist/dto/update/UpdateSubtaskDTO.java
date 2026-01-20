package com.lmlasmo.tasklist.dto.update;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSubtaskDTO {

	@JsonProperty
	@Size(max = 128)
	@Pattern(regexp = ".*\\S.*", message = "Not can blank")
	private String name;
	
	@JsonProperty
	@Size(max = 2048)
	@Pattern(regexp = ".*\\S.*", message = "Not can blank")
	private String summary;
	
	@JsonProperty
	@Min(1)
	private Integer durationMinutes;
	
}
