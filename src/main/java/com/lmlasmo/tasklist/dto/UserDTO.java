package com.lmlasmo.tasklist.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.RoleType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO implements VersionedDTO {	
	
	@JsonProperty
	private int id;
	
	@JsonProperty
	private String username;
	
	@JsonProperty	
	private RoleType role;
	
	@JsonProperty
	@JsonInclude(content = Include.NON_NULL)
	private Instant lastLogin;
	
	@JsonProperty
	private Instant createdAt;
	
	@JsonProperty
	private Instant updatedAt;
	
	@JsonProperty
	private long version;

}
