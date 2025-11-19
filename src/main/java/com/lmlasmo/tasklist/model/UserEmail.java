package com.lmlasmo.tasklist.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_emails")
public class UserEmail {

	@Id
	private int id;
	
	@Column
	private String email;
	
	@Column("is_primary")
	private boolean primary = false;
	
	@Column
	private EmailStatusType status;
	
	@Column("row_version")
	@Version
	private long version;
	
	@ReadOnlyProperty
	@Column("created_at")
	private Instant createdAt;
	
	@ReadOnlyProperty
	@Column("updated_at")
	private Instant updatedAt;
	
	private int userId;
	
	public UserEmail(String email) {
		this.email = email;
	}
	
	public UserEmail(String email, int userId) {
		this(email);
		this.userId = userId;
	}
	
}
