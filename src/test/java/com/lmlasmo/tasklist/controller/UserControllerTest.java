package com.lmlasmo.tasklist.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.lmlasmo.tasklist.TaskListApplicationTests;
import com.lmlasmo.tasklist.data.tool.AuthTestTool;

@TestInstance(Lifecycle.PER_CLASS)
public class UserControllerTest extends TaskListApplicationTests {
	
	@Autowired
	private WebTestClient webTClient;
	
	@Autowired
	private AuthTestTool authTestTool;
	
	private final String baseUri = "/api/user";
	
	@Test
	void deleteUser() {
		String baseUri = this.baseUri + "/i";
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.delete()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(accessToken))
			.exchange()
			.expectStatus().isNoContent();
		});
	}
	
	@Test
	void unauthorizedDeleteUser() {
		String baseUri = this.baseUri + "/i";
		
		authTestTool.runWithUniqueAuth(at -> {
			webTClient.delete()
			.uri(baseUri)
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@Test
	void findByI() {
		String baseUri = this.baseUri + "/i";
		
		authTestTool.runWithUniqueAuth(at -> {
			String accessToken = at.getTokens().getAccessToken().getToken();
			
			webTClient.get()
			.uri(baseUri)
			.headers(h -> h.setBearerAuth(accessToken))
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.id").isNumber()
			.jsonPath("$.username").isNotEmpty()
			.jsonPath("$.role").isNotEmpty()
			.jsonPath("$.lastLogin").isNotEmpty();			
		});
	}
	
	@Test
	void unauthorizedFindByI() {
		String baseUri = this.baseUri + "/i";
		
		authTestTool.runWithUniqueAuth(at -> {
			webTClient.get()
			.uri(baseUri)
			.exchange()
			.expectStatus().isUnauthorized();			
		});
	}
	
}
