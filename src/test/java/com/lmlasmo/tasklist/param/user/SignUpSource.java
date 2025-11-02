package com.lmlasmo.tasklist.param.user;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface SignUpSource {

	public static Stream<Arguments> source(){
		String randomPassword = UUID.randomUUID().toString();
		return Stream.of(
				Arguments.of(new SignUpData("username", "test@example.com", randomPassword, 201, null)),
				Arguments.of(new SignUpData("username", "test@example.com", "", 400, MethodArgumentNotValidException.class)),
				Arguments.of(new SignUpData("username", "test@example.com", "1234567", 400, MethodArgumentNotValidException.class)),
				Arguments.of(new SignUpData("", "test@example.com", randomPassword, 400, MethodArgumentNotValidException.class)),
				Arguments.of(new SignUpData("username","testexample.com", randomPassword, 400, MethodArgumentNotValidException.class)),
				Arguments.of(new SignUpData("username","", randomPassword, 400, MethodArgumentNotValidException.class))
				);
	}

	@AllArgsConstructor
	@Getter
	static class SignUpData {

		private String username;
		private String email;
		private String password;
		private int status;
		private Class<? extends Exception> expectedException;

	}

}
