package com.lmlasmo.tasklist.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.lmlasmo.tasklist.dto.create.CreateUserDTO;

import io.r2dbc.spi.Readable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "users")
public class User implements UserDetails{

	private static final long serialVersionUID = -7660617497542184786L;
	
	@Id
	@NonNull
	private Integer id;
	
	@Column
	private String username;
	
	@Column
	private String password;
		
	@Column
	private RoleType role = RoleType.COMUM;
		
	@Column
	private UserStatusType status = UserStatusType.ACTIVE;
	
	@ReadOnlyProperty
	@Column("created_at")
	private Instant createdAt;
	
	@ReadOnlyProperty
	@Column("updated_at")
	private Instant updatedAt;
		
	@Column("last_login")
	private Instant lastLogin;
	
	@Column("row_version")
	@Version
	private long version;
	
	public User(CreateUserDTO signup) {
		this.username = signup.getUsername();
		this.password = signup.getPassword();
	}
	
	public User(Readable row) {
		this.id = row.get("id", Integer.class);
		this.username = row.get("username", String.class);
		this.password = row.get("password", String.class);
		this.role = RoleType.valueOf(row.get("role", String.class));
		this.status = UserStatusType.valueOf(row.get("status", String.class));
		this.version = row.get("row_version", Long.class);
		
		this.createdAt = Optional.ofNullable(row.get("created_at", LocalDateTime.class))
				.map(i -> i.toInstant(ZoneOffset.UTC))
				.orElse(null);
		
		this.updatedAt = Optional.ofNullable(row.get("updated_at", LocalDateTime.class))
				.map(i -> i.toInstant(ZoneOffset.UTC))
				.orElse(null);
		
		this.lastLogin = Optional.ofNullable(row.get("last_login", LocalDateTime.class))
				.map(i -> i.toInstant(ZoneOffset.UTC))
				.orElse(null);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {		
		return List.of(RoleType.values()).stream().map(RoleType::name).map(SimpleGrantedAuthority::new).toList();		
	}	

}
