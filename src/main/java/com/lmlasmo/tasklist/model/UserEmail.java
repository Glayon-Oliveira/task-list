package com.lmlasmo.tasklist.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_emails")
public class UserEmail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column
	private String email;
	
	@Column(name = "is_primary")
	private boolean primary = false;
	
	@Enumerated(EnumType.STRING)
	@Column
	private EmailStatusType status;
	
	@Column(name = "row_version")
	@Version
	private long version;
	
	@CreationTimestamp
	@Column(name = "created_at")
	private Instant createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User user;
	
	public UserEmail(String email) {
		this.email = email;
	}
	
}
