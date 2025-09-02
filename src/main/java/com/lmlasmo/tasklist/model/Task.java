package com.lmlasmo.tasklist.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;

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
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NonNull
	private Integer id;
	
	@Column
	private String name;
	
	@Column
	private String summary;
	
	@Column
	private Instant deadline;
	
	@Column(name = "deadline_zone", length = 50)
	private String deadlineZone;
	
	@CreationTimestamp
	@Column(name = "created_at")
	private Instant createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
	
	@Enumerated(EnumType.STRING)
	@Column	
	private TaskStatusType status = TaskStatusType.PENDING;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToMany(mappedBy = "task")	
	private Set<Subtask> subtasks = new HashSet<>();
	
	public Task(CreateTaskDTO create, User user) {
		this.name = create.getName();
		this.summary = create.getSummary();
		this.deadline = create.getDeadline().toInstant();
		this.deadlineZone = create.getDeadlineZone();
		this.user = user;
	}

}
