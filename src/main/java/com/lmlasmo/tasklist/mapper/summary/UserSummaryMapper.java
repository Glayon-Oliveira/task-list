package com.lmlasmo.tasklist.mapper.summary;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.lmlasmo.tasklist.model.RoleType;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.summary.Field;
import com.lmlasmo.tasklist.repository.summary.UserSummary;

import io.r2dbc.spi.Readable;

@Mapper(componentModel = "spring")
public interface UserSummaryMapper extends SummaryMapper {
	
	@Mappings({
		@Mapping(target = "id", expression = "java(unwrap(\"id\", user.getId(), includedFields))"),
	    @Mapping(target = "username", expression = "java(unwrap(\"username\", user.getUsername(), includedFields))"),
	    @Mapping(target = "password", expression = "java(unwrap(\"password\", user.getPassword(), includedFields))"),
	    @Mapping(target = "role", expression = "java(unwrap(\"role\", user.getRole(), includedFields))"),
	    @Mapping(target = "status", expression = "java(unwrap(\"status\", user.getStatus(), includedFields))"),	    
	    @Mapping(target = "lastLogin", expression = "java(unwrap(\"lastLogin\", user.getLastLogin(), includedFields))"),
	    @Mapping(target = "version", expression = "java(unwrap(\"version\", user.getVersion(), includedFields))"),
	    @Mapping(target = "createdAt", expression = "java(unwrap(\"createdAt\", user.getCreatedAt(), includedFields))"),
	    @Mapping(target = "updatedAt", expression = "java(unwrap(\"updatedAt\", user.getUpdatedAt(), includedFields))"),
	})
	public UserSummary toSummary(User user, Set<String> includedFields);
	
	default UserSummary toSummary(Readable row, Set<String> includedFields) {
		Field<Integer> id = includedFields.contains("id")
				? Field.of(row.get("id", Integer.class))
				: Field.absent();
		
		Field<String> username = includedFields.contains("username")
				? Field.of(row.get("username", String.class))
				: Field.absent();
		
		Field<String> password = includedFields.contains("password")
				? Field.of(row.get("password", String.class))
				: Field.absent();
		
		Field<RoleType> role = includedFields.contains("role")
				? Field.of(RoleType.valueOf(row.get("role", String.class)))
				: Field.absent();
		
		Field<UserStatusType> status = includedFields.contains("status")
				? Field.of(UserStatusType.valueOf(row.get("status", String.class)))
				: Field.absent();
		
		Field<Instant> lastLogin = includedFields.contains("lastLogin")
				? Field.of(
						Optional.ofNullable(row.get("lastLogin", LocalDateTime.class))
						.map(i -> i.toInstant(ZoneOffset.UTC))
						.orElse(null)
						)
				: Field.absent();
		
		Field<Long> version = includedFields.contains("version")
				? Field.of(row.get("version", Long.class))
				: Field.absent();
		
		Field<Instant> createdAt = includedFields.contains("createdAt")
				? Field.of(
						Optional.ofNullable(row.get("created_at", LocalDateTime.class))
						.map(i -> i.toInstant(ZoneOffset.UTC))
						.orElse(null)
						)
				: Field.absent();
		
		Field<Instant> updatedAt = includedFields.contains("updatedAt")
				? Field.of(
						Optional.ofNullable(row.get("updated_at", LocalDateTime.class))
						.map(i -> i.toInstant(ZoneOffset.UTC))
						.orElse(null)
						)
				: Field.absent();
		
		return new UserSummary(
				id, username, password, role, status, lastLogin, 
				version, createdAt, updatedAt
				);
	}
	
}
