package com.lmlasmo.tasklist.repository.summary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class SubtaskSummary implements BasicSummary<Integer> {
	
	public static final Set<String> REQUIRED_FIELDS = Stream.concat(
			BasicSummary.REQUIRED_FIELDS.stream(), 
			Stream.of("taskId"))
			.collect(Collectors.toSet());
	
	public static final Set<String> FIELDS = Stream.concat(
			REQUIRED_FIELDS.stream(), 
			Stream.of("name", "summary", "status", "position", "durationMinutes"))
			.collect(Collectors.toSet());
		
	private Field<Integer> id;
	private Field<String> name;
	private Field<String> summary;
	private Field<TaskStatusType> status;	
	private Field<BigDecimal> position;
	
	private Field<Integer> durationMinutes;
	
	private Field<Long> version;
	private Field<Instant> createdAt;
	private Field<Instant> updatedAt;
	private Field<Integer> taskId;

}
