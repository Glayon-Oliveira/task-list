package com.lmlasmo.tasklist.model;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {		
		return List.of(RoleType.values()).stream().map(RoleType::name).map(SimpleGrantedAuthority::new).toList();		
	}	

}
