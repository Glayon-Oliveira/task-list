package com.lmlasmo.tasklist.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.model.Task;

@Mapper(componentModel = "spring")
public interface TaskMapper {

	@Mapping(target = "deadline", expression = "java(mapDeadline(task.getDeadline(), task.getDeadlineZone()))")
	TaskDTO toDTO(Task task);
	
	@Mappings({
		@Mapping(target = "id", ignore = true),
		@Mapping(target = "status", ignore = true),
		@Mapping(target = "createdAt", ignore = true),
		@Mapping(target = "updatedAt", ignore = true),
		@Mapping(target = "version", ignore = true),
		@Mapping(target = "userId", ignore = true),
		@Mapping(target = "deadline", expression = "java(mapDeadline(create.getDeadline()))")
	})
	Task toEntity(CreateTaskDTO create);
	
	default OffsetDateTime mapDeadline(Instant deadline, String deadlineZone) {
		 if (deadline == null) return null;
		 
		 return deadline.atZone(ZoneId.of(deadlineZone)).toOffsetDateTime();
	}
	
	default Instant mapDeadline(OffsetDateTime deadline) {
		 if (deadline == null) return null;
		 
		 return deadline.toInstant();
	}
	
}
