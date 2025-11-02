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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.lmlasmo.tasklist.param.FailureAuthenticateEndpointSource;
import com.lmlasmo.tasklist.service.SubtaskService;
import com.lmlasmo.tasklist.service.TaskService;
import com.lmlasmo.tasklist.service.TaskStatusService;
import com.lmlasmo.tasklist.service.UserEmailService;
import com.lmlasmo.tasklist.util.VerifyResolvedException;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest
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

	@Override
	@BeforeEach
	protected void setUp() {
		super.setUp();

		when(getUserService().existsById(anyInt())).thenReturn(false);
		when(getUserService().findById(anyInt())).thenThrow(EntityNotFoundException.class);
	}

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.FailureAuthenticateEndpointSource#source")
	void authenticateEndpoint(FailureAuthenticateEndpointSource.FailureAuthenticateEndpointData data) throws Exception {
		for(HttpMethod method: data.getMethods()) {
			getMockMvc().perform(MockMvcRequestBuilders.request(method, data.getEndpoint()))
			.andExpect(MockMvcResultMatchers.status().is(401));
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

			getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
					.contentType(MediaType.APPLICATION_JSON)
					.content(signIn))
			.andExpect(MockMvcResultMatchers.status().is(401))
			.andExpect(result -> VerifyResolvedException.verify(result, BadCredentialsException.class));
		}
	}

}
