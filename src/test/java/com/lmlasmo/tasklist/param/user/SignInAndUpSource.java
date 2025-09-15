package com.lmlasmo.tasklist.param.user;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface SignInAndUpSource {

	public static Stream<Arguments> source(){
		String randomPassword = UUID.randomUUID().toString();
		return Stream.of(
				Arguments.of(new SignInAndUpData("username", randomPassword, 201, null)),
				Arguments.of(new SignInAndUpData("username", "", 400, MethodArgumentNotValidException.class)),
				Arguments.of(new SignInAndUpData("username", "1234567", 400, MethodArgumentNotValidException.class)),
				Arguments.of(new SignInAndUpData("", randomPassword, 400, MethodArgumentNotValidException.class))
				);
	}

	@AllArgsConstructor
	@Getter
	static class SignInAndUpData {

		private String username;
		private String password;
		private int status;
		private Class<? extends Exception> expectedException;

	}

}
