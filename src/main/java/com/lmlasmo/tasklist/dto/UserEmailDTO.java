package com.lmlasmo.tasklist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.model.UserEmail;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEmailDTO {

	@JsonProperty
	private String email;
	
	@JsonProperty
	private boolean primary;
	
	@JsonProperty
	private EmailStatusType status;
	
	@JsonProperty
	private long version;
	
	public UserEmailDTO(UserEmail email) {
		this.email = email.getEmail();
		this.status = email.getStatus();
		this.primary = email.isPrimary();
		this.version = email.getVersion();
	}
	
}
