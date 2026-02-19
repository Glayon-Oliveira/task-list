package com.lmlasmo.tasklist.mapper.summary;

import java.math.BigDecimal;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.BasicSubtaskSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.StatusSummary;

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
	
	default BasicSubtaskSummary toSubtaskSummary(int id, long version, int taskId) {
		return new BasicSubtaskSummary() {
			
			@Override
			public int getId() {return id;}
			
			@Override
			public long getVersion() {return version;}
			
			@Override
			public int getTaskId() {return taskId;}
			
		};
	}
	
	default BasicSubtaskSummary toSubtaskSummary(Readable row) {
		return toSubtaskSummary(
				row.get("id", Integer.class),
				row.get("row_version", Long.class),
				row.get("task_Id", Integer.class)
				);
	}
	
	default PositionSummary toPositionSummary(int id, long version, int taskId, BigDecimal position) {
		return new PositionSummary() {
			
			@Override
			public int getId() {return id;}
			
			@Override
			public long getVersion() {return version;}
			
			@Override
			public int getTaskId() {return taskId;}
			
			@Override
			public BigDecimal getPosition() {return position;}
			
		};
	}
	
	default PositionSummary toPositionSummary(Readable row) {
		return toPositionSummary(
				row.get("id", Integer.class),
				row.get("row_version", Long.class),
				row.get("task_Id", Integer.class),
				row.get("position", BigDecimal.class)
				);
	}
	
	default StatusSummary toStatusSummary(int id, long version, int taskId, TaskStatusType status) {
		return new StatusSummary() {
			
			@Override
			public int getId() {return id;}
			
			@Override
			public long getVersion() {return version;}
			
			@Override
			public int getTaskId() {return taskId;}
			
			@Override
			public TaskStatusType getStatus() {return status;}
		};
	}
	
	default StatusSummary toStatusSummary(Readable row) {
		return toStatusSummary(
				row.get("id", Integer.class),
				row.get("row_version", Long.class),
				row.get("task_Id", Integer.class),
				TaskStatusType.valueOf(row.get("status", String.class))
				);
	}
	
}
