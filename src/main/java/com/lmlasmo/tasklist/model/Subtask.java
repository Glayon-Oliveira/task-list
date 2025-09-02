package com.lmlasmo.tasklist.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;

import jakarta.persistence.CascadeType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subtasks")
public class Subtask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column
	private String name;
	
	@Column
	private String summary;
	
	@Column(name = "duration_minutes")
	private int durationMinutes;
	
	@Enumerated(EnumType.STRING)
	@Column
	private TaskStatusType status = TaskStatusType.PENDING;
	
	@Column
	private int position;
	
	@CreationTimestamp
	@Column(name = "created_at")
	private Instant createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "task_id")
	private Task task;
	
	public Subtask(CreateSubtaskDTO create) {
		this.name = create.getName();
		this.durationMinutes = create.getDurationMinutes();
		this.summary = create.getSummary();
		this.task = new Task(create.getTaskId());
	}
	
}
