package com.lmlasmo.tasklist.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.create.CreateUserDTO;
import com.lmlasmo.tasklist.model.RoleType;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;

import io.r2dbc.spi.Readable;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserDTO toDTO(User user);
	
	@Mappings({
		@Mapping(target = "id", ignore = true),
		@Mapping(target = "role", ignore = true),
		@Mapping(target = "status", ignore = true),
		@Mapping(target = "createdAt", ignore = true),
		@Mapping(target = "updatedAt", ignore = true),
		@Mapping(target = "lastLogin", ignore = true),
		@Mapping(target = "version", ignore = true),
		@Mapping(target = "authorities", ignore = true),
	})
	User toUser(CreateUserDTO create);
	
	default User toUser(Readable row) {
		int id = row.get("id", Integer.class);
		String username = row.get("username", String.class);
		String password = row.get("password", String.class);
		RoleType role = RoleType.valueOf(row.get("role", String.class));
		UserStatusType status = UserStatusType.valueOf(row.get("status", String.class));
		long version = row.get("row_version", Long.class);
		
		Instant createdAt = Optional.ofNullable(row.get("created_at", LocalDateTime.class))
				.map(i -> i.toInstant(ZoneOffset.UTC))
				.orElse(null);
		
		Instant updatedAt = Optional.ofNullable(row.get("updated_at", LocalDateTime.class))
				.map(i -> i.toInstant(ZoneOffset.UTC))
				.orElse(null);
		
		Instant lastLogin = Optional.ofNullable(row.get("last_login", LocalDateTime.class))
				.map(i -> i.toInstant(ZoneOffset.UTC))
				.orElse(null);
		
		User user = new User();
		user.setId(id);
		user.setUsername(username);
		user.setPassword(password);
		user.setRole(role);
		user.setStatus(status);
		user.setVersion(version);
		user.setCreatedAt(createdAt);
		user.setUpdatedAt(updatedAt);
		user.setLastLogin(lastLogin);
		
		return user;
	}
	
}
