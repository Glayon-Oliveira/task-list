package com.lmlasmo.tasklist.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordRecoveryDTO extends PasswordDTO {
	
	@JsonProperty
	@Email
	@NotBlank
	private String email;

	@JsonProperty
	@NotNull
	private EmailConfirmationCodeHashDTO confirmation;
	
}
