package com.lmlasmo.tasklist.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "tasks")
public class Task {

	@Id
	@NonNull
	private Integer id;
	
	@Column
	private String name;
	
	@Column
	private String summary;
	
	@Column
	private Instant deadline;
	
	@Column("deadline_zone")
	private String deadlineZone;
	
	@ReadOnlyProperty
	@Column("created_at")
	private Instant createdAt;
	
	@ReadOnlyProperty
	@Column("updated_at")
	private Instant updatedAt;
	
	@Column	
	private TaskStatusType status = TaskStatusType.PENDING;
	
	@Column("row_version")
	@Version
	private long version;
	
	@Column("user_id")
	private int userId;

}
