package com.lmlasmo.tasklist.controller.auth;

import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.AuthController;
import com.lmlasmo.tasklist.dto.auth.JWTTokenType;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailService;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.UserEmailService;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {AuthController.class})
@Import(EmailConfirmationService.class)
@TestInstance(Lifecycle.PER_CLASS)
public class TokenJwtTest extends AbstractControllerTest {
	
	@MockitoBean
	private UserEmailService userEmailService;
	
	@MockitoBean
	private EmailService emailService;
	
	@MockitoBean
	private ResourceAccessService resourceAccess;
	
	@RepeatedTest(2)
	void successRefreshToken(RepetitionInfo info) throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token/refresh";
		
		RequestBodySpec reqspec = getWebTestClient().post().uri(baseUri);
		
		if(info.getCurrentRepetition() == 1) {
			reqspec = reqspec.cookie("rt", getDefaultRefreshJwtToken());
		}else if(info.getCurrentRepetition() == 2) {
			String token = """
					{
						"token": "%s"
					}
					""";
			
			reqspec = (RequestBodySpec) reqspec.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(String.format(token, getDefaultRefreshJwtToken()));
		}
		
		when(getUserService().lastLoginToNow(getDefaultUser().getId())).thenReturn(Mono.empty());
		
		ResponseSpec respec = getWebTestClient().post()
			.uri(baseUri)
			.cookie("rt", getDefaultRefreshJwtToken())
			.exchange();
		
		respec.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.refreshToken.token").exists()
			.jsonPath("$.refreshToken.type").isEqualTo(JWTTokenType.REFRESH)
			.jsonPath("$.refreshToken.duration").exists()
			.jsonPath("$.accessToken.token").exists()
			.jsonPath("$.accessToken.type").isEqualTo(JWTTokenType.ACCESS)
			.jsonPath("$.accessToken.duration").exists();
	}
	
	@Test
	void successAccessToken() throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token/access";
		
		when(getUserService().lastLoginToNow(getDefaultUser().getId())).thenReturn(Mono.empty());
		
		getWebTestClient().post().uri(baseUri)
			.cookie("rt", getDefaultRefreshJwtToken())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
				.jsonPath("$.token").exists()
				.jsonPath("$.type").isEqualTo(JWTTokenType.ACCESS)
				.jsonPath("$.duration").exists();
	}
	
	@RepeatedTest(2)
	void failureRefreshToken(RepetitionInfo info) throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token/refresh";
		String falseToken = UUID.randomUUID().toString();
		
		RequestBodySpec reqspec = getWebTestClient().post().uri(baseUri);
		
		if(info.getCurrentRepetition() == 1) {
			reqspec = reqspec.cookie("rt", falseToken);
		}else if(info.getCurrentRepetition() == 2) {
			String token = """
					{
						"token": "%s"
					}
					""";
			
			reqspec = (RequestBodySpec) reqspec.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(String.format(token, falseToken));
		}
		
		reqspec.exchange().expectStatus().isUnauthorized();
	}
	
	@Test
	void failureAccessToken() throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token/access";
		String falseToken = UUID.randomUUID().toString();
		
		String token = """
				"token": "%s"
				""";
		
		getWebTestClient().post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(String.format(token, falseToken))
			.exchange()
			.expectStatus().isUnauthorized();
	}
	
}
