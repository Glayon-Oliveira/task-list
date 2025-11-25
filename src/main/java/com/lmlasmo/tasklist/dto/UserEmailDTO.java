package com.lmlasmo.tasklist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.EmailStatusType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEmailDTO {
	
	@JsonProperty
	private int id;

	@JsonProperty
	private String email;
	
	@JsonProperty
	private boolean primary;
	
	@JsonProperty
	private EmailStatusType status;
	
	@JsonProperty
	private long version;
	
}
