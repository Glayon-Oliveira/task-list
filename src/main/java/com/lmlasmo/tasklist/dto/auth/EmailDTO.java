package com.lmlasmo.tasklist.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailDTO {

	@JsonProperty
	@NotBlank
	@Size(max = 255)
	@Email
	private String email;
	
	public void setEmail(String email) {
		if(email != null) {
			this.email = email.toLowerCase();
		}
	}
	
}
