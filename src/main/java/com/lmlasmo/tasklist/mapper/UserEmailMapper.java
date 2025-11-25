package com.lmlasmo.tasklist.mapper;

import org.mapstruct.Mapper;

import com.lmlasmo.tasklist.dto.UserEmailDTO;
import com.lmlasmo.tasklist.model.UserEmail;

@Mapper(componentModel = "spring")
public interface UserEmailMapper {

	UserEmailDTO toDTO(UserEmail userEmail);
	
}
