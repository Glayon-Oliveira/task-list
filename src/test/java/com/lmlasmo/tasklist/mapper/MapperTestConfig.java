package com.lmlasmo.tasklist.mapper;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MapperTestConfig {

	@Bean
	@Primary
	public UserMapper userMapper() {
		return Mappers.getMapper(UserMapper.class);
	}
	
	@Bean
	@Primary
	public UserEmailMapper userEmailMapper() {
		return Mappers.getMapper(UserEmailMapper.class);
	}
	
}
