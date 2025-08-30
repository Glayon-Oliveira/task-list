package com.lmlasmo.tasklist.model;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.lmlasmo.tasklist.dto.SignupDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails{

	private static final long serialVersionUID = -7660617497542184786L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NonNull
	private Integer id;
	
	@Column
	private String username;
	
	@Column
	private String password;
	
	@Enumerated(EnumType.STRING)
	@Column
	private RoleType role = RoleType.COMUM;
	
	@CreationTimestamp
	@Column(name = "created_at")
	private Instant createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
	
	@OneToMany(mappedBy = "user")
	private Set<Task> tasks;
	
	public User(SignupDTO signup) {
		this.username = signup.getUsername();
		this.password = signup.getPassword();		
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {		
		return List.of(RoleType.values()).stream().map(RoleType::name).map(SimpleGrantedAuthority::new).toList();		
	}	

}
