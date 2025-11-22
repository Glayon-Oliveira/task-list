package com.lmlasmo.tasklist.dto.update;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubtaskPositionDTO {

	@JsonProperty
	@NotNull
	private MovePositionType moveType = MovePositionType.AFTER;
	
	@JsonProperty
	@Min(1)
	private int anchorSubtaskId;
	
	public static enum MovePositionType {
		AFTER,
		BEFORE;
	}
	
}
