package com.lmlasmo.tasklist.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

@Mapper(componentModel = "spring")
public interface SubtaskMapper {

	SubtaskDTO toDTO(Subtask subtask);
	
	SubtaskDTO toDTO(SubtaskSummary subtask);
	
	@Mappings({
		@Mapping(target = "id", ignore = true),
		@Mapping(target = "status", ignore = true),
		@Mapping(target = "position", ignore = true),
		@Mapping(target = "createdAt", ignore = true),
		@Mapping(target = "updatedAt", ignore = true),
		@Mapping(target = "version", ignore = true),
	})
	Subtask toEntity(CreateSubtaskDTO create);
	
}
