package com.lmlasml.tasklist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasml.tasklist.model.RoleType;
import com.lmlasml.tasklist.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {	
	
	@JsonProperty
	private int id;
	
	@JsonProperty
	private String username;
	
	@JsonProperty	
	private RoleType role;
	
	public UserDTO(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.role = user.getRole();
	}

}
