package com.lmlasmo.tasklist.repository.summary;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lmlasmo.tasklist.model.RoleType;
import com.lmlasmo.tasklist.model.UserStatusType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UserSummary implements BasicSummary<Integer>{
	
	public static final Set<String> REQUIRED_FIELDS = Stream.concat(
			BasicSummary.REQUIRED_FIELDS.stream(), 
			Stream.of())
			.collect(Collectors.toSet());
	
	public static final Set<String> FIELDS = Stream.concat(
			REQUIRED_FIELDS.stream(), 
			Stream.of("username", "password", "role", "status", "lastLogin"))
			.collect(Collectors.toSet());
	
	private Field<Integer> id;
	private Field<String> username;
	private Field<String> password;
	private Field<RoleType> role;
	private Field<UserStatusType> status;
	private Field<Instant> lastLogin;
	private Field<Long> version;
	private Field<Instant> createdAt;
	private Field<Instant> updatedAt;
	
	@Override
	public Map<String, Object> toMap() {
		Map<String,Object> map = new LinkedHashMap<>(BasicSummary.super.toMap());
		map.remove("password");
		
		return map;
	}
	
}
