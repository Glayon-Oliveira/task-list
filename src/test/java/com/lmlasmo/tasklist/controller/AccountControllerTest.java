package com.lmlasmo.tasklist.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.lmlasmo.tasklist.TaskListApplicationTests;
import com.lmlasmo.tasklist.data.tool.AuthTestTool;
import com.lmlasmo.tasklist.data.tool.UserEmailTestTool;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationCodeHashDTO;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationHashDTO;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;

@TestInstance(Lifecycle.PER_CLASS)
public class AccountControllerTest extends TaskListApplicationTests {
	
	@Autowired
	private WebTestClient webTClient;
	
	@Autowired
	private UserEmailTestTool userEmailTestTool;
	
	@Autowired
	private AuthTestTool authTestTool;
	
	@Autowired
	private EmailConfirmationService emailConfirmationService;
	
	private final String baseUri = "/api/account";
	
	@Test
	void linkEmail() {
		String baseUri = this.baseUri + "/email/link";
		String newEmailLinked = "newemaillinked@example.test";
		
		EmailConfirmationCodeHashDTO confirmation = emailConfirmationService.createCodeHash(
				newEmailLinked, 
				EmailConfirmationScope.LINK
				).block();
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("email", newEmailLinked);
		body.put("confirmation", confirmation);
		
		authTestTool.runWithUniqueAuth(at -> {
			userEmailTestTool.runWithUser(at.getUser(), uem -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.post()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isNoContent();
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
				"newEmailLinked@example.test",
				"newEmailLinkedexample.test",
				"newEmailLinked@",
				"@example.test",
				"null"
			}
	)
	void failedLinkEmailWithInvalidEmailValue(String email) {
		String baseUri = this.baseUri + "/email/link";
		
		EmailConfirmationHashDTO confirmation = emailConfirmationService.createCodeHash(
				email, 
				EmailConfirmationScope.LINK
				).block();
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("email",email);
		body.put("confirmation", confirmation);
		
		authTestTool.runWithUniqueAuth(at -> {
			userEmailTestTool.runWithUser(at.getUser(), uem -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.post()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
		nullValues = "null",
		value = {
			"NO_CODE, NO_HASH, '2025-01-01T12:30:00Z'",
			"'', NO_HASH, '2025-01-01T12:30:00Z'",
			"null, NO_HASH, '2025-01-01T12:30:00Z'",
			"NO_CODE, '', '2025-01-01T12:30:00Z'",
			"NO_CODE, null, '2025-01-01T12:30:00Z'",
			"NO_CODE, NO_HASH, '2025-01-01T12:30:00Z'",
			"NO_CODE, NO_HASH, ''",
			"NO_CODE, NO_HASH, null",
		}
	)
	void failedLinkEmailWithInvalidBodyCodeHashValues(String code, String hash, String timestamp) {
		String baseUri = this.baseUri + "/email/link";
		
		Map<String, Object> confirmation = new LinkedHashMap<>();
		confirmation.put("code", code);
		confirmation.put("hash", hash);
		confirmation.put("timestamp", timestamp);
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("email", "newEmailLinked@example.test");
		body.put("confirmation", confirmation);
		
		authTestTool.runWithUniqueAuth(at -> {
			userEmailTestTool.runWithUser(at.getUser(), uem -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.post()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange()
				.expectStatus().isBadRequest();
			});
		});
	}
	
	@Test
	void unauthorizedCreateUserWithInvalidBodyCodeHashValues() {
		String baseUri = this.baseUri + "/email/link";
		
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getRefreshToken().getToken();
			
			webTClient.post()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(token))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(Map.of())
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@Test
	void changePrimaryEmail() {
		String baseUri = this.baseUri + "/email/primary/{id}";
		
		Set<String> emails = Set.of("firstEmail@example.test", "secondEmail@example.test"); 
		
		authTestTool.runWithUniqueAuth(at -> {
			userEmailTestTool.runWithUserAndEmails(at.getUser(), emails, uems -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				List<UserEmail> userEmails = List.copyOf(uems);
				
				webTClient.patch()
				.uri(baseUri, userEmails.get(1).getId())
				.headers(h -> h.setBearerAuth(accessToken))
				.exchange()
				.expectStatus().isNoContent();
			});
		});
	}
	
	@Test
	void failedChangePrimaryEmailWithNotFound() {
		String baseUri = this.baseUri + "/email/primary/{id}"; 
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.patch()
			.uri(baseUri, Integer.MAX_VALUE)
			.headers(h -> h.setBearerAuth(accessToken))
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
	@Test
	void unauthorizedChangePrimaryEmail() {
		String baseUri = this.baseUri + "/email/primary/{id}"; 
		
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getRefreshToken().getToken();
			
			webTClient.patch()
			.uri(baseUri, Integer.MAX_VALUE)
			.headers(h -> h.setBearerAuth(token))
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@Test
	void terminateEmail() {
		String baseUri = this.baseUri + "/email/terminate"; 
		
		authTestTool.runWithUniqueAuth(at -> {
			userEmailTestTool.runWithUser(at.getUser(), uem -> {
				String accessToken = at.getTokens().getAccessToken().getToken();
				
				webTClient.post()
				.uri(baseUri)
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(Map.of("email", uem.getEmail()))
				.exchange()
				.expectStatus().isNoContent();
			});
		});
	}
	
	@ParameterizedTest
	@CsvSource(
		nullValues = "null",
		value = {
			"fakeEmailexample.test",
			"''",
			"null",
		}
	)
	void failedTerminateEmailWithInvalidEmailValue(String email) {
		String baseUri = this.baseUri + "/email/terminate";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("email", email);
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.post()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(accessToken))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest();
		});
	}
	
	@Test
	void failedTerminateEmailWithNotFound() {
		String baseUri = this.baseUri + "/email/terminate"; 
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.post()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(accessToken))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(Map.of("email", "fakeEmail@example.test"))
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
	@Test
	void unauthorizedTerminateEmailWithNotFound() {
		String baseUri = this.baseUri + "/email/terminate"; 
		
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getRefreshToken().getToken();
			
			webTClient.post()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(token))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(Map.of())
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
}
