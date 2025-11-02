package com.lmlasmo.tasklist.param;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;;

public interface FailureAuthenticateEndpointSource {

	public static List<HttpMethod> getAllMethods() {
		return List.of(GET, POST, DELETE, PUT, OPTIONS, HEAD, PATCH);
	}
	
	public static Stream<Arguments> sourceForAccount() {
		return Stream.of(
				Arguments.of(new FailureAuthenticateEndpointData(List.of(POST), "/api/account/email/vincule")),
				Arguments.of(new FailureAuthenticateEndpointData(List.of(POST), "/api/account/email/terminate"))
				);
	}
	
	public static Stream<Arguments> sourceForAuth() {
		return Stream.of(
				Arguments.of(new FailureAuthenticateEndpointData(List.of(POST), "/api/auth/token/refresh")),
				Arguments.of(new FailureAuthenticateEndpointData(List.of(POST), "/api/auth/token/access"))
				);
	}

	public static Stream<Arguments> sourceForUser(){
		List<HttpMethod> methods = getAllMethods();
		return Stream.of(
				Arguments.of(new FailureAuthenticateEndpointData(methods, "/api/user"))
				);
	}

	public static Stream<Arguments> sourceForTask(){
		List<HttpMethod> methods = getAllMethods();
		return Stream.of(
				Arguments.of(new FailureAuthenticateEndpointData(methods, "/api/task")),
				Arguments.of(new FailureAuthenticateEndpointData(methods, "/api/task/")),
				Arguments.of(new FailureAuthenticateEndpointData(methods, "/api/task/1"))
				);
	}

	public static Stream<Arguments> sourceForSubtask(){
		List<HttpMethod> methods = getAllMethods();
		return Stream.of(
				Arguments.of(new FailureAuthenticateEndpointData(methods, "/api/subtask"))
				);
	}

	public static Stream<Arguments> source(){
		return Stream.of(
				sourceForAccount(),
				sourceForAuth(),
				sourceForUser(),
				sourceForTask(),
				sourceForSubtask()
				).flatMap(s -> s);
	}

	@Getter
	@AllArgsConstructor
	static class FailureAuthenticateEndpointData {
		private List<HttpMethod> methods;
		private String endpoint;
	}
}
