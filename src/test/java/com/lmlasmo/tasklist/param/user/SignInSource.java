package com.lmlasmo.tasklist.param.user;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class SignInSource {

	public static Stream<Arguments> source(){
		String randomPassword = UUID.randomUUID().toString();
		return Stream.of(
				Arguments.of(new SignInData("username", randomPassword, 201, null)),
				Arguments.of(new SignInData("username", "", 400, MethodArgumentNotValidException.class)),
				Arguments.of(new SignInData("username", "1234567", 400, MethodArgumentNotValidException.class)),
				Arguments.of(new SignInData("test@example.com", randomPassword, 201, null)),
				Arguments.of(new SignInData("", randomPassword, 400, MethodArgumentNotValidException.class))
				);
	}

	@AllArgsConstructor
	@Getter
	public static class SignInData {

		private String login;
		private String password;
		private int status;
		private Class<? extends Exception> expectedException;

	}
	
	
}
