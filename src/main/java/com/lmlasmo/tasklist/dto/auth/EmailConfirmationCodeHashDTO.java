package com.lmlasmo.tasklist.dto.auth;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
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

	@JsonProperty
	@NotBlank
	private String code;
	
	public EmailConfirmationCodeHashDTO(String hash, Instant timestamp, String code) {
		super(hash, timestamp);
		this.code = code;
	}
	
}
