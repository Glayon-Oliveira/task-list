package com.lmlasmo.tasklist.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DoubleJWTTokensDTO {

	@JsonProperty
	private JWTTokenDTO refreshToken;
	
	@JsonProperty
	private JWTTokenDTO accessToken;
	
}
