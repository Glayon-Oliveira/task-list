package com.lmlasmo.tasklist.param;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;;

public interface FailureAuthenticateEndpointSource {

	public static List<HttpMethod> getAllMethods() {
		return List.of(
				HttpMethod.GET,
				HttpMethod.POST,
				HttpMethod.DELETE,
				HttpMethod.PUT,
				HttpMethod.OPTIONS,
				HttpMethod.HEAD,
				HttpMethod.PATCH
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
