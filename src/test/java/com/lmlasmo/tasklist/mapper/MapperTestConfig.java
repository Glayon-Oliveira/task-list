package com.lmlasmo.tasklist.mapper;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.lmlasmo.tasklist.mapper.summary.SubtaskSummaryMapper;
import com.lmlasmo.tasklist.mapper.summary.TaskSummaryMapper;
import com.lmlasmo.tasklist.mapper.summary.UserSummaryMapper;

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
	
	@Bean
	@Primary
	public TaskMapper taskMapper() {
		return Mappers.getMapper(TaskMapper.class);
	}
	
	@Bean
	@Primary
	public SubtaskMapper subtaskMapper() {
		return Mappers.getMapper(SubtaskMapper.class);
	}
	
	@Bean
	@Primary
	public UserSummaryMapper userSummaryMapper() {
		return Mappers.getMapper(UserSummaryMapper.class);
	}
	
	@Bean
	@Primary
	public TaskSummaryMapper taskSummaryMapper() {
		return Mappers.getMapper(TaskSummaryMapper.class);
	}
	
	@Bean
	@Primary
	public SubtaskSummaryMapper subtaskSummaryMapper() {
		return Mappers.getMapper(SubtaskSummaryMapper.class);
	}
	
}
