package com.lmlasmo.tasklist.dto.update;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDescriptionTaskDTO {

	@JsonProperty
	@Size(max = 255)
	@Pattern(regexp = ".*\\S.*", message = "Not can blank")
	private String name;
	
	@JsonProperty
	@Pattern(regexp = ".*\\S.*", message = "Not can blank")
	private String summary;
	
}
