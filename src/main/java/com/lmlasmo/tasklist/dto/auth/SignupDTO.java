package com.lmlasmo.tasklist.dto.auth;

import com.lmlasmo.tasklist.dto.create.CreateUserDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupDTO extends CreateUserDTO {
	
	private EmailConfirmationCodeHashDTO confirmation;
	
}
