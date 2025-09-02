package com.lmlasmo.tasklist.exception;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponseDTO {
	
	@JsonProperty
	private int status;
	
	@JsonProperty
	private String error;
	
	@JsonProperty
	private String message;
	
	@JsonProperty
	private String path;
	
	@JsonProperty
	private LocalDateTime timestamp;

}
