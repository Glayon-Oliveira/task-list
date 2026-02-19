package com.lmlasmo.tasklist.mapper.summary;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

import io.r2dbc.spi.Readable;

@Mapper(componentModel = "spring")
public interface TaskSummaryMapper extends SummaryMapper {
	
	@Mappings({
		@Mapping(target = "id", expression = "java(unwrap(\"id\", task.getId(), includedFields))"),
	    @Mapping(target = "name", expression = "java(unwrap(\"name\", task.getName(), includedFields))"),
	    @Mapping(target = "summary", expression = "java(unwrap(\"summary\", task.getSummary(), includedFields))"),
	    @Mapping(target = "status", expression = "java(unwrap(\"status\", task.getStatus(), includedFields))"),
	    @Mapping(target = "deadline", expression = "java(unwrap(\"deadline\", task.getDeadline(), includedFields))"),
	    @Mapping(target = "deadlineZone", expression = "java(unwrap(\"deadlineZone\", task.getDeadlineZone(), includedFields))"),
	    @Mapping(target = "version", expression = "java(unwrap(\"version\", task.getVersion(), includedFields))"),
	    @Mapping(target = "createdAt", expression = "java(unwrap(\"createdAt\", task.getCreatedAt(), includedFields))"),
	    @Mapping(target = "updatedAt", expression = "java(unwrap(\"updatedAt\", task.getUpdatedAt(), includedFields))"),
	    @Mapping(target = "userId", expression = "java(unwrap(\"userId\", task.getUserId(), includedFields))")
	})
	public TaskSummary toSummary(Task task, Set<String> includedFields);
	
	default StatusSummary toStatusSummary(int id, long version, TaskStatusType status) {
		return new StatusSummary() {
			
			@Override
			public int getId() {return id;}
			
			@Override
			public long getVersion() {return version;}
			
			@Override
			public TaskStatusType getStatus() {return status;}
		};
	}
	
	default StatusSummary toStatusSummary(Readable row) {
		return toStatusSummary(
				row.get("id", Integer.class),
				row.get("row_version", Long.class),
				TaskStatusType.valueOf(row.get("status", String.class))
				);
	}
	
}
