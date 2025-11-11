package com.lmlasmo.tasklist.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.param.FailureAuthenticateEndpointSource;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailService;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;
import com.lmlasmo.tasklist.service.UserEmailService;

import reactor.core.publisher.Mono;

@WebFluxTest
@TestInstance(Lifecycle.PER_CLASS)
public class FailureAuthenticationControllerTest extends AbstractControllerTest {

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private SubtaskService subtaskService;

	@MockitoBean
	private TaskStatusService statusService;
	
	@MockitoBean
	private UserEmailService userEmailService;
	
	@MockitoBean
	private EmailService emailService;
	
	@MockitoBean
	private EmailConfirmationService confirmationService;
	
	@MockitoBean
	private ResourceAccessService resourceAccess;

	@Override
	@BeforeEach
	protected void setUp() {
		super.setUp();

		when(getUserService().existsById(anyInt())).thenReturn(Mono.just(false));
		when(getUserService().findById(anyInt())).thenThrow(ResourceNotFoundException.class);
	}

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.FailureAuthenticateEndpointSource#source")
	void authenticateEndpoint(FailureAuthenticateEndpointSource.FailureAuthenticateEndpointData data) throws Exception {
		for(HttpMethod method: data.getMethods()) {
			getWebTestClient().method(method)
				.uri(data.getEndpoint())
				.exchange()
				.expectStatus().isUnauthorized();
		}
	}

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.FailureAuthenticateEndpointSource#source")
	void signIn() throws Exception {
		String baseUri = "/api/auth/login";
		String signInFormat = """
					{
							"login": "%s",
							"password": "%s"
					}
				""";

		List<Entry<String, String>> credentials = List.of(
				Map.entry(getDefaultUser().getUsername(), UUID.randomUUID().toString()),
				Map.entry(UUID.randomUUID().toString(), getDefaultPassword()),
				Map.entry(UUID.randomUUID().toString(), UUID.randomUUID().toString())
				);

		for(Entry<String, String> credential: credentials) {

			String signIn = String.format(signInFormat, credential.getKey(), credential.getValue());
			
			getWebTestClient().post()
				.uri(baseUri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(signIn)
				.exchange()
				.expectStatus().isUnauthorized();
		}
	}

}
