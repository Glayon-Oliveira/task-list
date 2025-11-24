package com.lmlasmo.tasklist.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JWTTokenDTO extends TokenDTO {

	@JsonProperty
	@JsonInclude(content = Include.NON_NULL)
	private JWTTokenType type;
	
	@JsonProperty
	@JsonInclude(content = Include.NON_DEFAULT)
	private long duration;
	
	public JWTTokenDTO(String token, JWTTokenType type, long duration) {
		super(token);
		this.type = type;
		this.duration = duration;
	}	
	
	public static enum JWTTokenType {
		ACCESS,
		REFRESH
	}
	
}
