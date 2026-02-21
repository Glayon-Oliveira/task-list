package com.lmlasmo.tasklist.mapper.summary;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.Field;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

import io.r2dbc.spi.Readable;

@Mapper(componentModel = "spring")
public interface SubtaskSummaryMapper extends SummaryMapper {
	
	@Mappings({
        @Mapping(target = "id", expression = "java(unwrap(\"id\", subtask.getId(), includedFields))"),
        @Mapping(target = "name", expression = "java(unwrap(\"name\", subtask.getName(), includedFields))"),
        @Mapping(target = "summary", expression = "java(unwrap(\"summary\", subtask.getSummary(), includedFields))"),
        @Mapping(target = "status", expression = "java(unwrap(\"status\", subtask.getStatus(), includedFields))"),
        @Mapping(target = "position", expression = "java(unwrap(\"position\", subtask.getPosition(), includedFields))"),
        @Mapping(target = "durationMinutes", expression = "java(unwrap(\"durationMinutes\", subtask.getDurationMinutes(), includedFields))"),
        @Mapping(target = "version", expression = "java(unwrap(\"version\", subtask.getVersion(), includedFields))"),
        @Mapping(target = "createdAt", expression = "java(unwrap(\"createdAt\", subtask.getCreatedAt(), includedFields))"),
        @Mapping(target = "updatedAt", expression = "java(unwrap(\"updatedAt\", subtask.getUpdatedAt(), includedFields))"),
        @Mapping(target = "taskId", expression = "java(unwrap(\"taskId\", subtask.getTaskId(), includedFields))")
    })
    SubtaskSummary toSummary(Subtask subtask, Set<String> includedFields);
	
	default SubtaskSummary toSummary(Readable row, Set<String> includedFields) {
		Field<Integer> id = includedFields.contains("id")
				? Field.of(row.get("id", Integer.class))
				: Field.absent();
		
		Field<String> name = includedFields.contains("name")
				? Field.of(row.get("name", String.class))
				: Field.absent();
		
		Field<String> summary = includedFields.contains("summary")
				? Field.of(row.get("summary", String.class))
				: Field.absent();
		
		Field<TaskStatusType> status = includedFields.contains("status")
				? Field.of(TaskStatusType.valueOf(row.get("status", String.class)))
				: Field.absent();
		
		Field<BigDecimal> position = includedFields.contains("position")
				? Field.of(row.get("position", BigDecimal.class))
				: Field.absent();
		
		Field<Integer> durationMinutes = includedFields.contains("durationMinutes")
				? Field.of(row.get("durationMinutes", Integer.class))
				: Field.absent();
		
		Field<Integer> taskId = includedFields.contains("task_id")
				? Field.of(row.get("task_id", Integer.class))
				: Field.absent();
		
		Field<Long> version = includedFields.contains("version")
				? Field.of(row.get("version", Long.class))
				: Field.absent();
		
		Field<Instant> createdAt = includedFields.contains("createdAt")
				? Field.of(
						Optional.ofNullable(row.get("created_at", LocalDateTime.class))
						.map(i -> i.toInstant(ZoneOffset.UTC))
						.orElse(null)
						)
				: Field.absent();
		
		Field<Instant> updatedAt = includedFields.contains("updatedAt")
				? Field.of(
						Optional.ofNullable(row.get("updated_at", LocalDateTime.class))
						.map(i -> i.toInstant(ZoneOffset.UTC))
						.orElse(null)
						)
				: Field.absent();
		
		return new SubtaskSummary(
				id, name, summary, status, position, durationMinutes, 
				version, createdAt, updatedAt, taskId
				);
	}
	
}
