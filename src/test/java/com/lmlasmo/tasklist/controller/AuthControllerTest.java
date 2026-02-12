package com.lmlasmo.tasklist.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.lmlasmo.tasklist.TaskListApplicationTests;
import com.lmlasmo.tasklist.data.tool.AuthTestTool;
import com.lmlasmo.tasklist.data.tool.UserEmailTestTool;
import com.lmlasmo.tasklist.data.tool.UserTestTool;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationCodeHashDTO;
import com.lmlasmo.tasklist.dto.auth.EmailWithScope;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO.JWTTokenType;
import com.lmlasmo.tasklist.dto.auth.LoginDTO;
import com.lmlasmo.tasklist.dto.auth.PasswordRecoveryDTO;
import com.lmlasmo.tasklist.dto.auth.SignupDTO;
import com.lmlasmo.tasklist.dto.auth.TokenDTO;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.repository.UserRepository;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;

@TestInstance(Lifecycle.PER_CLASS)
public class AuthControllerTest extends TaskListApplicationTests {
	
	@Autowired
	private WebTestClient webTClient;
	
	@Autowired
	private UserTestTool userTestTool;
	
	@Autowired
	private UserEmailTestTool userEmailTestTool;
	
	@Autowired
	private AuthTestTool authTestTool;
	
	@Autowired
	private EmailConfirmationService emailConfirmationService;
	
	@Autowired
	private UserRepository userRepository;
	
	private final String baseUri = "/api/auth";
	
	@BeforeEach
	protected void cleanUsers() {
		userRepository.deleteAll().block();
	}
	
	@ParameterizedTest
	@CsvSource({
		"Username72378, email920@example.test, 873582002"
	})
	void createUser(String username, String email, String password) {
		String baseUri = this.baseUri + "/signup";
		
		EmailConfirmationCodeHashDTO codeHash = emailConfirmationService.createCodeHash(
				email, 
				EmailConfirmationScope.LINK
				).block();
		
		SignupDTO signup = new SignupDTO();
		signup.setUsername(username);
		signup.setEmail(email);
		signup.setPassword(password);
		signup.setConfirmation(codeHash);
		
		webTClient.post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(signup)
			.exchange()
			.expectStatus().is2xxSuccessful()
			.expectBody()
			.jsonPath("$.username").isEqualTo(username);
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
					"'', email920@example.test, 873582002",
					"'    ', email920@example.test, 873582002",
					"'null', email920@example.test, 873582002",
					"Username72378, '', 873582002",
					"Username72378, '     ', 873582002",
					"Username72378, email920@example.test, ''",
					"Username72378, email920@example.test, '    '",
					"Username72378, email920@example.test, 'null'",
					"Username72378, email920example.test, 873582002",
					"Username72378, email920@example.test, 8735820"
			}
	)
	void failedCreateUserWithInvalidBodyValues(String username, String email, String password) {
		String baseUri = this.baseUri + "/signup";
		
		EmailConfirmationCodeHashDTO codeHash = emailConfirmationService.createCodeHash(
				email, 
				EmailConfirmationScope.LINK
				).block();
		
		SignupDTO signup = new SignupDTO();
		signup.setUsername(username);
		signup.setEmail(email);
		signup.setPassword(password);
		signup.setConfirmation(codeHash);
		
		webTClient.post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(signup)
			.exchange()
			.expectStatus().isBadRequest();
	}
	
	@RepeatedTest(3)
	void failedCreateUserWithInvalidBodyCodeHashValues(RepetitionInfo repInfo) {
		String baseUri = this.baseUri + "/signup";
				
		EmailConfirmationCodeHashDTO codeHash = new EmailConfirmationCodeHashDTO();
		
		switch(repInfo.getCurrentRepetition()) {
			case 1: 
				codeHash.setCode(""); 
				codeHash.setHash("NO HASH");
				codeHash.setTimestamp(Instant.now());
				break;
			case 2:
				codeHash.setHash("");
				codeHash.setCode("NO CODE");
				codeHash.setTimestamp(Instant.now());
				break;
			case 3:
				codeHash.setTimestamp(null);
				codeHash.setCode("NO CODE"); 
				codeHash.setHash("NO HASH");
				break;
		}
		
		SignupDTO signup = new SignupDTO();
		signup.setUsername(UUID.randomUUID().toString());
		signup.setEmail("email982710@example.test");
		signup.setPassword(UUID.randomUUID().toString());
		signup.setConfirmation(codeHash);
		
		webTClient.post()
		.uri(baseUri)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(signup)
		.exchange()
		.expectStatus().isBadRequest();
	}
	
	@RepeatedTest(4)
	void loginWithEmail(RepetitionInfo repInfo) {
		String baseUri = this.baseUri + "/login";
		
		String username = "usghjkd";
		String email = "email08728@example.test";
		String password = "htjkddj6yw";
		
		String loginValue = repInfo.getCurrentRepetition() % 2 == 0
				? username
				: email;
		
		userTestTool.runWithCredentials(
				username, password,
				u -> {
					Consumer<UserEmail> consumerUserEmail = uem -> {
						LoginDTO login = new LoginDTO();
						login.setLogin(loginValue);
						login.setPassword(password);
						
						webTClient.post()
						.uri(baseUri)
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(login)
						.exchange()
						.expectStatus().isOk()
						.expectBody()
						.jsonPath("$.duration").exists()
						.jsonPath("$.token").exists()
						.jsonPath("$.type").isEqualTo(JWTTokenType.ACCESS);
					};
					
					if(loginValue.contains("@")) {
						userEmailTestTool.runWithUserAndEmail(u, email, consumerUserEmail);
					}else {
						userEmailTestTool.runWithUser(u, consumerUserEmail);
					}
				});
	}
	
	@Test
	void loginWithRefreshTokenInBody() {
		String baseUri = this.baseUri + "/login";
		
		String username = "usghjkd";
		String password = "htjkddj6yw";
		
		userTestTool.runWithCredentials(
				username, password,
				u -> {
					LoginDTO login = new LoginDTO();
					login.setLogin(username);
					login.setPassword(password);
					
					webTClient.post()
					.uri(baseUri)
					.header("X-RefreshToken-Provider", "")
					.contentType(MediaType.APPLICATION_JSON)
					.bodyValue(login)
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$.accessToken").exists()
					.jsonPath("$.accessToken.duration").exists()
					.jsonPath("$.accessToken.token").exists()
					.jsonPath("$.accessToken.type").isEqualTo(JWTTokenType.ACCESS)
					.jsonPath("$.refreshToken").exists()
					.jsonPath("$.refreshToken.duration").isNumber()
					.jsonPath("$.refreshToken.token").isNotEmpty()
					.jsonPath("$.refreshToken.type").isEqualTo(JWTTokenType.REFRESH);
				});
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
					"'', password",
					"'null', password",
					"username, ''",
					"username, '     '",
					"username, 'null'",
			}
	)
	void failedLoginWithInvalidBodyValues(String username, String password) {
		String baseUri = this.baseUri + "/login";
		
		LoginDTO login = new LoginDTO();
		login.setLogin(username);
		login.setPassword(password);
		
		String headerForRefreshTokenInBody = "X-RefreshToken-Provider";
		
		for(int cc = 0; cc < 2; cc++) {
			if(cc == 1) {
				headerForRefreshTokenInBody = "X-NO-RT";
			}
			
			webTClient.post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.header(headerForRefreshTokenInBody, "")
			.bodyValue(login)
			.exchange()
			.expectStatus().isBadRequest();
		}
	}
	
	@RepeatedTest(2)
	void failedLogin(RepetitionInfo repInfo) {
		String baseUri = this.baseUri + "/login";
		
		String loginValue = repInfo.getCurrentRepetition() % 2 == 0
				? "usghjkd"
				: "email08728@example.test";
		
		String password = "htjkddj6yw";
		
		LoginDTO login = new LoginDTO();
		login.setLogin(loginValue);
		login.setPassword(password);
		
		webTClient.post()
		.uri(baseUri)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(login)
		.exchange()
		.expectStatus().isUnauthorized();
	}
	
	@Test
	void regenerateRefreshTokenWithBody() {
		String baseUri = this.baseUri + "/token/refresh";
		
		authTestTool.runWithUniqueAuth(at -> {
			TokenDTO refreshToken = at.getTokens().getRefreshToken();
			
			webTClient.post()
				.uri(baseUri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(refreshToken)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.refreshToken.token").isNotEmpty()
				.jsonPath("$.refreshToken.duration").isNumber()
				.jsonPath("$.refreshToken.type").isEqualTo(JWTTokenType.REFRESH)
				.jsonPath("$.accessToken.token").isNotEmpty()
				.jsonPath("$.accessToken.duration").isNumber()
				.jsonPath("$.accessToken.type").isEqualTo(JWTTokenType.ACCESS);
		});
	}
	
	@Test
	void regenerateRefreshTokenWithCookie() {
		String baseUri = this.baseUri + "/token/refresh";
		
		authTestTool.runWithUniqueAuth(at -> {
			String refreshToken = at.getTokens().getRefreshToken().getToken();
			
			webTClient.post()
				.uri(baseUri)
				.cookie("rt", refreshToken)
				.exchange()
				.expectStatus().isOk()
				.expectCookie()
				.exists("rt")
				.expectBody()
				.jsonPath("$.token").isNotEmpty()
				.jsonPath("$.duration").isNumber()
				.jsonPath("$.type").isEqualTo(JWTTokenType.ACCESS);
		});
	}
	
	@Test
	void failedRegenerateRefreshTokenWithBody() {
		String baseUri = this.baseUri + "/token/refresh";
		
		authTestTool.runWithUniqueAuth(at -> {
			TokenDTO token = new TokenDTO("");
			
			webTClient.post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(token)
			.exchange()
			.expectStatus().isBadRequest();
			
			token = at.getTokens().getAccessToken();
			
			webTClient.post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(token)
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@Test
	void failedRegenerateRefreshTokenWithCookie() {
		String baseUri = this.baseUri + "/token/refresh";
		
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getAccessToken().getToken();
			
			webTClient.post()
				.uri(baseUri)
				.cookie("rt", token)
				.exchange()
				.expectStatus().isUnauthorized();
		});
	}
	
	@Test
	void generateAccessTokenWithBody() {
		String baseUri = this.baseUri + "/token/access";
		
		authTestTool.runWithUniqueAuth(at -> {
			TokenDTO refreshToken = at.getTokens().getRefreshToken();
			
			webTClient.post()
				.uri(baseUri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(refreshToken)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.token").isNotEmpty()
				.jsonPath("$.duration").isNumber()
				.jsonPath("$.type").isEqualTo(JWTTokenType.ACCESS);
		});
	}
	
	@Test
	void generateAccessTokenWithCookie() {
		String baseUri = this.baseUri + "/token/access";
		
		authTestTool.runWithUniqueAuth(at -> {
			String refreshToken = at.getTokens().getRefreshToken().getToken();
			
			webTClient.post()
				.uri(baseUri)
				.cookie("rt", refreshToken)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.token").isNotEmpty()
				.jsonPath("$.duration").isNumber()
				.jsonPath("$.type").isEqualTo(JWTTokenType.ACCESS);
		});
	}
	
	@Test
	void failedGenerateAccessTokenWithBody() {
		String baseUri = this.baseUri + "/token/access";
		
		authTestTool.runWithUniqueAuth(at -> {
			TokenDTO token = new TokenDTO("");
			
			webTClient.post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(token)
			.exchange()
			.expectStatus().isBadRequest();
			
			token = at.getTokens().getAccessToken();
			
			webTClient.post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(token)
			.exchange()
			.expectStatus().isUnauthorized();
		});
	}
	
	@Test
	void failedGenerateAccessTokenWithCookie() {
		String baseUri = this.baseUri + "/token/refresh";
		
		authTestTool.runWithUniqueAuth(at -> {
			String token = at.getTokens().getAccessToken().getToken();
			
			webTClient.post()
				.uri(baseUri)
				.cookie("rt", token)
				.exchange()
				.expectStatus().isUnauthorized();
		});
	}
	
	@ParameterizedTest
	@CsvSource({
		"email9728@example.test, LINK",
		"email978e@example.test, RECOVERY"
	})
	void requestCodeConfirmationByEmail(String email, String scope) {
		String baseUri = this.baseUri + "/email/confirmation";
		
		EmailWithScope emailDto = new EmailWithScope();
		emailDto.setEmail(email);
		emailDto.setScope(EmailConfirmationScope.valueOf(scope));
		
		webTClient.post()
		.uri(baseUri)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(emailDto)
		.exchange()
		.expectStatus().isOk()
		.expectBody()
		.jsonPath("$.hash").isNotEmpty()
		.jsonPath("$.timestamp")
			.value(t -> assertDoesNotThrow(
					() -> Instant.parse(t.toString())
				))
		.jsonPath("$.code").doesNotHaveJsonPath();	
	}
	
	@ParameterizedTest
	@CsvSource(
			nullValues = "null",
			value = {
					"email9728example.test, LINK",
					"'', LINK",
					"'null', LINK",
					"email9728@example.test, ''",
					"email9728@example.test, LINKS",
					"email9728@example.test, RECOVERIES",
					}
	)
	void failedRequestCodeConfirmationByEmailWithInvalidBody(String email, String scope) throws JsonMappingException, JsonProcessingException {
		String baseUri = this.baseUri + "/email/confirmation";
		
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("email", email);
		body.put("scope", scope);
		
		webTClient.post()
		.uri(baseUri)
		.contentType(MediaType.APPLICATION_JSON)
		.bodyValue(body)
		.exchange()
		.expectStatus().isBadRequest();
	}
	
	@Test
	void failedRequestCodeConfirmationByEmailWithConflit() {
		String baseUri = this.baseUri + "/email/confirmation";
		
		userEmailTestTool.runWithUniqueEmail(uem -> {
			EmailWithScope emailDto = new EmailWithScope();
			emailDto.setEmail(uem.getEmail());
			emailDto.setScope(EmailConfirmationScope.LINK);
			
			webTClient.post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(emailDto)
			.exchange()
			.expectStatus().isEqualTo(409);
		});	
	}
	
	@Test
	void requestRecoverPassword() {
		String baseUri = this.baseUri + "/recover/password";
		
		userEmailTestTool.runWithUniqueEmail(uem -> {
			EmailConfirmationCodeHashDTO codeHash = emailConfirmationService
					.createCodeHash(uem.getEmail(), EmailConfirmationScope.RECOVERY)
					.block();
			
			PasswordRecoveryDTO recoveryDto = new PasswordRecoveryDTO();
			recoveryDto.setEmail(uem.getEmail());
			recoveryDto.setPassword("password");
			recoveryDto.setConfirmation(codeHash);
			
			webTClient.patch()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(recoveryDto)
			.exchange()
			.expectStatus().isNoContent();
		});
	}
	
	@Test
	void failedRequestRecoverPassword() {
		String baseUri = this.baseUri + "/recover/password";
		
		userEmailTestTool.runWithUniqueEmail(uem -> {
			String fakeEmail = "email00841@example.test";
			
			EmailConfirmationCodeHashDTO recoveryCodeHashWithFakeEmail = emailConfirmationService
					.createCodeHash(fakeEmail, EmailConfirmationScope.RECOVERY)
					.block();
			
			PasswordRecoveryDTO recoveryDtoWithFakeEmail = new PasswordRecoveryDTO();
			recoveryDtoWithFakeEmail.setEmail(fakeEmail);
			recoveryDtoWithFakeEmail.setPassword("password");
			recoveryDtoWithFakeEmail.setConfirmation(recoveryCodeHashWithFakeEmail);
			
			EmailConfirmationCodeHashDTO linkCodeHash = emailConfirmationService
					.createCodeHash(uem.getEmail(), EmailConfirmationScope.RECOVERY)
					.block();
			
			PasswordRecoveryDTO recoveryDtoWithLinkHash = new PasswordRecoveryDTO();
			recoveryDtoWithLinkHash.setEmail(fakeEmail);
			recoveryDtoWithLinkHash.setPassword("password");
			recoveryDtoWithLinkHash.setConfirmation(linkCodeHash);
			
			webTClient.patch()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(recoveryDtoWithLinkHash)
			.exchange()
			.expectStatus().isBadRequest();
			
			webTClient.patch()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(recoveryDtoWithFakeEmail)
			.exchange()
			.expectStatus().isNotFound();
		});
	}
	
}
