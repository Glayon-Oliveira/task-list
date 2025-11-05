package com.lmlasmo.tasklist.dto.auth;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
public class EmailConfirmationCodeHashDTO extends EmailConfirmationHashDTO{

	private String code;
	
	public EmailConfirmationCodeHashDTO(String hash, Instant timestamp, String code) {
		super(hash, timestamp);
		this.code = code;
	}
	
}
