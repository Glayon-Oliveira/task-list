package com.lmlasmo.tasklist.model;

import java.time.LocalDateTime;

import com.lmlasmo.tasklist.dto.TaskDTO;

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
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column
	private String name;
	
	@Column
	private String task;
	
	@Column
	private LocalDateTime timestamp = LocalDateTime.now();
	
	@Enumerated(EnumType.STRING)
	@Column	
	private TaskStatusType status = TaskStatusType.PENDING;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User user;
	
	public Task(TaskDTO create) {
		this.name = create.getName();
		this.task = create.getTask();
		this.user = new User(create.getUserId());
	}

}
