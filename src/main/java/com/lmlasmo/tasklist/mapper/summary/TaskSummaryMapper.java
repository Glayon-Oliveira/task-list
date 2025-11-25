package com.lmlasmo.tasklist.mapper.summary;

import org.mapstruct.Mapper;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

import io.r2dbc.spi.Readable;

@Mapper(componentModel = "spring")
public interface TaskSummaryMapper {
	
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
