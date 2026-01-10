package com.lmlasmo.tasklist.controller.auth;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec;

import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.AuthController;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO.JWTTokenType;
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
		
		BodyContentSpec bodySpec = reqspec.exchange()
				.expectStatus().isOk()
				.expectBody();
			
		if(info.getCurrentRepetition() == 1) {
			bodySpec.jsonPath("$.token").exists()
			.jsonPath("$.type").isEqualTo(JWTTokenType.ACCESS)
			.jsonPath("$.duration").exists();
		}else if(info.getCurrentRepetition() == 2){
			bodySpec.jsonPath("$.refreshToken.token").exists()
			.jsonPath("$.refreshToken.type").isEqualTo(JWTTokenType.REFRESH)
			.jsonPath("$.refreshToken.duration").exists();
		}
	}
	
	@RepeatedTest(2)
	void successAccessToken(RepetitionInfo info) throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token/access";
		
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
		
		reqspec.exchange()
			.expectStatus().isOk()
			.expectBody()
				.jsonPath("$.token").exists()
				.jsonPath("$.type").isEqualTo(JWTTokenType.ACCESS)
				.jsonPath("$.duration").exists();
	}
	
	@RepeatedTest(3)
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
		
		if(info.getCurrentRepetition() == 3) {
			reqspec.exchange().expectStatus().isBadRequest();
			assumeFalse(true);
		}
		
		reqspec.exchange().expectStatus().isUnauthorized();
	}
	
	@RepeatedTest(3)
	void failureAccessToken(RepetitionInfo info) throws UnsupportedEncodingException, Exception {
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
		
		if(info.getCurrentRepetition() == 3) {
			reqspec.exchange().expectStatus().isBadRequest();
			assumeFalse(true);
		}
		
		reqspec.exchange().expectStatus().isUnauthorized();
	}
	
}
