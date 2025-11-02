package com.lmlasmo.tasklist.dto;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmlasmo.tasklist.model.RoleType;
import com.lmlasmo.tasklist.model.User;

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
	private Set<UserEmailDTO> emails = new HashSet<>();
	
	@JsonProperty
	private Instant createdAt;
	
	@JsonProperty
	private Instant updatedAt;
	
	@JsonProperty
	private long version;
	
	public UserDTO(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.role = user.getRole();
		this.createdAt = user.getCreatedAt();
		this.updatedAt = user.getUpdatedAt();
		this.version = user.getVersion();
				
		user.getEmails().stream()
			.map(UserEmailDTO::new)
			.forEach(emails::add);
	}

}
