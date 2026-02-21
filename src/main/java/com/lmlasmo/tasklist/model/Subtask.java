package com.lmlasmo.tasklist.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "subtasks")
public class Subtask {

	@Id
	private Integer id;
	
	@Column
	private String name;
	
	@Column
	private String summary;
	
	@Column("duration_minutes")
	private Integer durationMinutes;
	
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
	private Long version;
	
	@Column("task_id")
	private Integer taskId;
	
}
