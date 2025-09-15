package com.lmlasmo.tasklist.param.user;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface UpdatePasswordOfDefaultUserSource {

	public static Stream<Arguments> source(){
		return Stream.of(
				Arguments.of(new UpdatePasswordOfDefaultUserData(UUID.randomUUID().toString(), 204, null)),
				Arguments.of(new UpdatePasswordOfDefaultUserData("", 400, MethodArgumentNotValidException.class)),
				Arguments.of(new UpdatePasswordOfDefaultUserData("1234567", 400, MethodArgumentNotValidException.class))
				);
	}

	@Getter
	@AllArgsConstructor
	static class UpdatePasswordOfDefaultUserData {
		private String password;
		private int status;
		private Class<? extends Exception> expectedException;
	}

}
