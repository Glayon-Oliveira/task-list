package com.lmlasmo.tasklist.param.task;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface CreateTaskSource {

	public static Stream<Arguments> source(){
		OffsetDateTime deadline = OffsetDateTime.now().plusMinutes(5);
		String zoneId = ZoneId.systemDefault().toString();
		String name = "Parameterized Test";
		String summary = "Test with parmas";

		return Stream.of(
				Arguments.of(new CreateTaskData(name, summary, deadline.toString(), zoneId, 201, null)),
				Arguments.of(new CreateTaskData(name, summary, deadline.minusMinutes(10).toString(), zoneId, 400, MethodArgumentNotValidException.class)),
				Arguments.of(new CreateTaskData(name, summary, "2030-09-04-T02:49:29.0Z", zoneId, 400, HttpMessageNotReadableException.class)),
				Arguments.of(new CreateTaskData(name, summary, deadline.toString(), "Marte/" + UUID.randomUUID(), 400, MethodArgumentNotValidException.class)),
				Arguments.of(new CreateTaskData(name, "", deadline.toString(), zoneId, 201, null)),
				Arguments.of(new CreateTaskData("", summary, deadline.toString(), zoneId, 400, MethodArgumentNotValidException.class))
				);
	}

	@Getter
	@AllArgsConstructor
	static class CreateTaskData {
		private String name;
		private String summary;
		private String deadline;
		private String deadlineZone;
		private int status;
		private Class<? extends Exception> expectedException;
	}

}
