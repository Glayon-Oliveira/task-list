package com.lmlasmo.tasklist.repository.summary;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Table("tasks")
public class TaskSummary {
	
	public static final Set<String> FIELDS = Set.of(
			"id", "name", "summary", "status", "deadline", "deadlineZone",
			"version", "createdAt", "updatedAt"
			);
	
	private int id;
	private String name;
	private String summary;
	private TaskStatusType status;
	private Instant deadline;
	
	@Column("deadline_zone") private String deadlineZone;
	
	@Column("row_version") private Long version;
	@Column("created_at") private Instant createdAt;
	@Column("updated_at") private Instant updatedAt;
	@Column("user_id") private Integer userId;
	
	public static interface StatusSummary extends BasicSummary {
		
		public TaskStatusType getStatus();
		
	}
	
}
