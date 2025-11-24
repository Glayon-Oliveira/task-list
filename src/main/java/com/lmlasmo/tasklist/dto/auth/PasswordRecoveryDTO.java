package com.lmlasmo.tasklist.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordRecoveryDTO extends PasswordDTO {
	
	@JsonProperty
	@Email
	@Size(max = 255)
	@NotBlank
	private String email;

	@JsonProperty
	@NotNull
	@Valid
	private EmailConfirmationCodeHashDTO confirmation;
	
}
