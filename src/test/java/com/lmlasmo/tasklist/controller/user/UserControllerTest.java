package com.lmlasmo.tasklist.controller.user;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.AuthController;
import com.lmlasmo.tasklist.controller.UserController;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.auth.DoubleJWTTokensDTO;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationCodeHashDTO;
import com.lmlasmo.tasklist.dto.create.CreateUserDTO;
import com.lmlasmo.tasklist.exception.ResourceAlreadyExistsException;
import com.lmlasmo.tasklist.param.user.SignInSource;
import com.lmlasmo.tasklist.param.user.SignUpSource;
import com.lmlasmo.tasklist.param.user.UpdatePasswordOfDefaultUserSource;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;
import com.lmlasmo.tasklist.service.EmailService;
import com.lmlasmo.tasklist.service.ResourceAccessService;
import com.lmlasmo.tasklist.service.UserEmailService;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {AuthController.class, UserController.class})
@Import(EmailConfirmationService.class)
@TestInstance(Lifecycle.PER_CLASS)
public class UserControllerTest extends AbstractControllerTest {

	@Autowired
	private ObjectMapper oMapper;
	
	@MockitoBean
	private UserEmailService userEmailService;
	
	@MockitoBean
	private EmailService emailService;
	
	@MockitoBean
	private ResourceAccessService resourceAccess;
	
	@Autowired
	private EmailConfirmationService confirmationService;

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.user.SignUpSource#source")
	void signUp(SignUpSource.SignUpData data) throws Exception {
		String createFormat = """
					{
							"username": "%s",
							"password": "%s",
							"email": "%s",
							"confirmation": {
								"code": "%s",
								"hash": "%s",
								"timestamp": "%s"
							}
					}
				""";
		
		when(userEmailService.existsByEmail(anyString())).thenReturn(Mono.just(false));
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash(data.getEmail(), EmailConfirmationScope.LINK).block();

		String create = String.format(
				createFormat,
				data.getUsername(),
				data.getPassword(), 
				data.getEmail(),
				codeHash.getCode(),
				codeHash.getHash(),
				codeHash.getTimestamp());
		
		String signUpUri = "/api/auth/signup";

		when(getUserService().save(any(CreateUserDTO.class))).thenReturn(Mono.just(new UserDTO(getDefaultUser())));
		when(userEmailService.existsByEmail(data.getEmail())).thenReturn(Mono.just(false));
		
		ResponseSpec response = getWebTestClient().post()
				.uri(signUpUri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(create)
				.exchange()
				.expectStatus().isEqualTo(data.getStatus());

		assumeTrue(data.getStatus() >= 200 && data.getStatus() < 300);
		
		response.expectBody()
			.jsonPath("$.username").isEqualTo(getDefaultUser().getUsername());
	}

	@ParameterizedTest	
	@MethodSource("com.lmlasmo.tasklist.param.user.SignInSource#source")
	void signIn(SignInSource.SignInData data) throws Exception {
		String baseUri = "/api/auth/login";
		
		String signInFormat = """
				{
						"login": "%s",
						"password": "%s"
				}
			""";

		assumeTrue(data.getStatus() < 200 || data.getStatus() >= 300);

		String signIn = String.format(signInFormat, data.getLogin(), data.getPassword());
		
		getWebTestClient().post()
			.uri(baseUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(signIn)
			.exchange()
			.expectStatus().isEqualTo(data.getStatus());
	}

	@RepeatedTest(2)
	void successSignIn(RepetitionInfo info) throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/login";
		String signInFormat = """
					{
							"login": "%s",
							"password": "%s"
					}
				""";
		
		int current = info.getCurrentRepetition();
		
		String email = "test@example.com";
		
		String signIn = String.format(signInFormat, (current % 2 == 0) ? getDefaultUser().getUsername() : email, getDefaultPassword());
		
		when(getUserService().lastLoginToNow(getDefaultUser().getId())).thenReturn(Mono.empty());
		
		byte[] responseBody = getWebTestClient().post()
				.uri(baseUri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(signIn)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.returnResult()
				.getResponseBody();
		
		DoubleJWTTokensDTO doublejwtTokens = oMapper.readValue(responseBody, DoubleJWTTokensDTO.class);
		
		getWebTestClient().get()
			.uri("/api/user/i")
			.header("Authorization", "Bearer " + doublejwtTokens.getAccessToken().getToken())
			.exchange()
			.expectStatus().isOk()
			.expectBody().jsonPath("$.username").isEqualTo(getDefaultUser().getUsername());
	}

	@RepeatedTest(2)
	void signUpConflitedWithDefaultUser(RepetitionInfo info) throws Exception {
		String signupFormat = """
					{
							"username": "%s",
							"email": "%s",
							"password": "%s",							
							"confirmation": {
								"code": "%s",
								"hash": "%s",
								"timestamp": "%s"
							}
					}
				""";
		String email = "test@example.com";
		
		when(userEmailService.existsByEmail(anyString())).thenReturn(Mono.just(false));
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash(email, EmailConfirmationScope.LINK).block();
		
		String signup = String.format(
				signupFormat,
				getDefaultUser().getUsername(),
				email,
				getDefaultPassword(),
				codeHash.getCode(),
				codeHash.getHash(),
				codeHash.getTimestamp());
		
		String signUpUri = "/api/auth/signup";

		when(getUserService().save(any())).thenThrow(ResourceAlreadyExistsException.class);
		
		if(info.getCurrentRepetition() % 2 == 1) when(userEmailService.existsByEmail(email)).thenReturn(Mono.just(false));
		
		getWebTestClient().post()
			.uri(signUpUri)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(signup)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void deleteDefaultUser() throws Exception {
		String baseUri = "/api/user/i";
		
		String etag = Long.toString(getDefaultUser().getVersion());
		String fEtag = Long.toString(getDefaultUser().getVersion()/2+1);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());		
		headers.setIfMatch("\""+etag+"\"");
		
		when(getUserService().existsByIdAndVersion(anyInt(), anyLong())).thenReturn(Mono.just(false));
		when(getUserService().existsByIdAndVersion(eq(getDefaultUser().getId()), eq(getDefaultUser().getVersion()))).thenReturn(Mono.just(true));
		when(getUserService().delete(anyInt())).thenReturn(Mono.empty());
		
		getWebTestClient().delete()
			.uri(baseUri)
			.headers(h -> h.addAll(headers))
			.exchange()
			.expectStatus().isNoContent();
		
		headers.setIfMatch("\""+fEtag+"\"");
		
		getWebTestClient().delete()
			.uri(baseUri)
			.headers(h -> h.addAll(headers))
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.PRECONDITION_FAILED);
	}

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.user.UpdatePasswordOfDefaultUserSource#source")
	void updatePasswordOfDefaultUser(UpdatePasswordOfDefaultUserSource.UpdatePasswordOfDefaultUserData data) throws Exception {
		String updateFormat = """
					{
							"email": "%s",
							"password": "%s",
							"confirmation": {
								"code": "%s",
								"hash": "%s",
								"timestamp": "%s"
							}
					}
				""";
		
		String email = "test@example.com";
		
		when(getUserService().updatePassword(any())).thenReturn(Mono.empty());
		when(userEmailService.existsByEmail(anyString())).thenReturn(Mono.just(true));
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash(email, EmailConfirmationScope.RECOVERY).block();

		String update = String.format(updateFormat,
				email,
				data.getPassword(),
				codeHash.getCode(),
				codeHash.getHash(),
				codeHash.getTimestamp());
		
		String updateUri = "/api/auth/recover/password";
		
		getWebTestClient().patch()
			.uri(updateUri)
			.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(update)
			.exchange()
			.expectStatus().isEqualTo(data.getStatus());
	}
	
	@Test
	void updatePasswordOfDefaultUser() throws Exception {
		String updateFormat = """
					{
							"email": "%s",
							"password": "%s",
							"confirmation": {
								"code": "%s",
								"hash": "%s",
								"timestamp": "%s"
							}
					}
				""";
		
		String email = "test@example.com";
		
		when(getUserService().updatePassword(any())).thenReturn(Mono.empty());
		when(userEmailService.existsByEmail(anyString())).thenReturn(Mono.just(true));
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash(email, EmailConfirmationScope.RECOVERY).block();

		String update = String.format(updateFormat,
				email,
				UUID.randomUUID(),
				codeHash.getCode(),
				codeHash.getHash(),
				codeHash.getTimestamp());
		
		String updateUri = "/api/auth/recover/password";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		
		when(getUserService().existsByIdAndVersion(getDefaultUser().getId(), getDefaultUser().getVersion())).thenReturn(Mono.just(true));
		
		getWebTestClient().patch()
			.uri(updateUri)
			.headers(h -> h.addAll(headers))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(update)
			.exchange()
			.expectStatus().isNoContent();
	}
	
	@Test
	void getI() throws Exception {
		String baseUri = "/api/user/i";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
				
		when(getUserService().existsByIdAndVersion(getDefaultUser().getId(), getDefaultUser().getVersion())).thenReturn(Mono.just(true));
		
		getWebTestClient().get()
			.uri(baseUri)
			.headers(h -> h.addAll(headers))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().exists("ETag")
			.expectBody().jsonPath("$.version").isEqualTo(getDefaultUser().getVersion());
		
		String etag = Long.toString(getDefaultUser().getVersion());
		headers.setIfNoneMatch("\""+etag+"\"");
		
		getWebTestClient().get()
			.uri(baseUri)
			.headers(h -> h.addAll(headers))
			.exchange()
			.expectStatus().isNotModified();
	}

}
