package com.lmlasmo.tasklist.dto.auth;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfirmationHashDTO {

	@JsonProperty
	@NotBlank
	private String hash;

	@JsonProperty
	@NotNull
	private Instant timestamp;

}
