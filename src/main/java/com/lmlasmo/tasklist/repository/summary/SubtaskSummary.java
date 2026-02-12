package com.lmlasmo.tasklist.repository.summary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@Table("subtasks")
public class SubtaskSummary {
	
	public static final Set<String> FIELDS = Set.of(
			"id", "name", "summary", "status", "position", "durationMinutes", "taskId",
			"version", "createdAt", "updatedAt"
			);
		
	private int id;
	private String name;
	private String summary;
	private TaskStatusType status = TaskStatusType.PENDING;	
	private BigDecimal position;
	
	@Column("duration_minutes") private int durationMinutes;
	@Column("created_at") private Instant createdAt;
	@Column("updated_at") private Instant updatedAt;	
	@Column("row_version") private long version;	
	@Column("task_id") private Long taskId;
		
	public static interface BasicSubtaskSummary extends BasicSummary {
		
		public int getTaskId();
		
	}
		
	public static interface PositionSummary extends BasicSubtaskSummary {
		
		public BigDecimal getPosition();
	}
		
	public static interface StatusSummary extends BasicSubtaskSummary{
		
		public TaskStatusType getStatus();
		public  int getTaskId();
		
	}

}
