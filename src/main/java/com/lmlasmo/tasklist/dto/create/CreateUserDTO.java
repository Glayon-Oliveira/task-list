package com.lmlasmo.tasklist.dto.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.dto.auth.PasswordDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserDTO extends PasswordDTO {

	@JsonProperty
	@NotBlank
	private String username;
	
	@JsonProperty
	@NotBlank
	@Email
	private String email;
	
	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}
	
}
