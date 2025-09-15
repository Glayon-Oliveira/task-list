package com.lmlasmo.tasklist.param.subtask;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface CreateSubtaskSource {

	public static Stream<Arguments> source(){
		String name = "SubtaskName-"+UUID.randomUUID().toString();
		String summary = "SubtaskSummary-"+UUID.randomUUID().toString();

		return Stream.of(
				Arguments.of(new CreateSubtaskData(name, summary, 5, 201, null)),
				Arguments.of(new CreateSubtaskData("", summary, 5, 400, MethodArgumentNotValidException.class)),
				Arguments.of(new CreateSubtaskData(name, "", 5, 201, null)),
				Arguments.of(new CreateSubtaskData(name, summary, 0, 400, MethodArgumentNotValidException.class))
				);
	}

	@Getter
	@AllArgsConstructor
	static class CreateSubtaskData {
		private String name;
		private String summary;
		private int durationMinutes;
		private int status;
		private Class<? extends Exception> expectedException;
	}

}
