package com.lmlasmo.tasklist.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailWithConfirmationDTO extends EmailDTO {

	@JsonProperty
	@NotNull
	@Valid
	private EmailConfirmationCodeHashDTO confirmation;
	
}
