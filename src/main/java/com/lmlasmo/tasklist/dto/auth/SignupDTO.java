package com.lmlasmo.tasklist.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.dto.create.CreateUserDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupDTO extends CreateUserDTO {
	
	@JsonProperty
	@NotNull
	@Valid
	private EmailConfirmationCodeHashDTO confirmation;
	
}
