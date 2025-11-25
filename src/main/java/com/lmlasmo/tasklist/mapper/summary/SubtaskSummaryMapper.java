package com.lmlasmo.tasklist.mapper.summary;

import java.math.BigDecimal;

import org.mapstruct.Mapper;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.BasicSubtaskSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.StatusSummary;

import io.r2dbc.spi.Readable;

@Mapper(componentModel = "spring")
public interface SubtaskSummaryMapper {
	
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
