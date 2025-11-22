package com.lmlasmo.tasklist.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "subtasks")
public class Subtask {

	@Id
	private int id;
	
	@Column
	private String name;
	
	@Column
	private String summary;
	
	@Column("duration_minutes")
	private int durationMinutes;
	
	@Column
	private TaskStatusType status = TaskStatusType.PENDING;
	
	@Column
	private BigDecimal position;
	
	@ReadOnlyProperty
	@Column("created_at")
	private Instant createdAt;
	
	@ReadOnlyProperty
	@Column("updated_at")
	private Instant updatedAt;
	
	@Column("row_version")
	@Version
	private long version;
	
	@Column("task_id")
	private int taskId;
	
	public Subtask(CreateSubtaskDTO create) {
		this.name = create.getName();
		this.durationMinutes = create.getDurationMinutes();
		this.summary = create.getSummary();
		this.taskId = create.getTaskId();
	}
	
}
