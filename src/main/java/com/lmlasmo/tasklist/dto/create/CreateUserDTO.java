package com.lmlasmo.tasklist.dto.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.dto.auth.PasswordDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserDTO extends PasswordDTO {

	@JsonProperty
	@NotBlank
	@Size(max = 255)
	private String username;
	
	@JsonProperty
	@Email
	@Size(max = 255)
	@NotBlank	
	private String email;
	
	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}
	
}
