package com.lmlasmo.tasklist.controller.user;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.AuthController;
import com.lmlasmo.tasklist.controller.UserController;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.auth.DoubleJWTTokensDTO;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationCodeHashDTO;
import com.lmlasmo.tasklist.dto.create.CreateUserDTO;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.param.user.SignInSource;
import com.lmlasmo.tasklist.param.user.SignUpSource;
import com.lmlasmo.tasklist.param.user.UpdatePasswordOfDefaultUserSource;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;
import com.lmlasmo.tasklist.service.EmailService;
import com.lmlasmo.tasklist.service.UserEmailService;
import com.lmlasmo.tasklist.util.VerifyResolvedException;

import jakarta.persistence.EntityExistsException;

@WebMvcTest(controllers = {AuthController.class, UserController.class})
@Import(EmailConfirmationService.class)
@TestInstance(Lifecycle.PER_CLASS)
public class UserControllerTest extends AbstractControllerTest {

	@Autowired
	private ObjectMapper oMapper;
	
	@MockitoBean
	private UserEmailService userEmailService;
	
	@MockitoBean
	private EmailService emailService;
	
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
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash(data.getEmail(), EmailConfirmationScope.LINK);

		String create = String.format(
				createFormat,
				data.getUsername(),
				data.getPassword(), 
				data.getEmail(),
				codeHash.getCode(),
				codeHash.getHash(),
				codeHash.getTimestamp());
		
		String signUpUri = "/api/auth/signup";

		when(getUserService().save(any(CreateUserDTO.class))).thenReturn(new UserDTO(getDefaultUser()));
		when(userEmailService.existsByEmail(data.getEmail())).thenReturn(false);		

		ResultActions resultActions = getMockMvc().perform(MockMvcRequestBuilders.post(signUpUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(create))
				.andExpect(MockMvcResultMatchers.status().is(data.getStatus()))
				.andExpect(result -> VerifyResolvedException.verify(result, data.getExpectedException()));

		assumeTrue(data.getStatus() >= 200 && data.getStatus() < 300);

		resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.username").value(getDefaultUser().getUsername()));
		resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.emails[0].email").value(getDefaultUser().getEmails().iterator().next().getEmail()));
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

		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(signIn))
		.andExpect(MockMvcResultMatchers.status().is(data.getStatus()))
		.andExpect(result -> VerifyResolvedException.verify(result, data.getExpectedException()));
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
		
		List<UserEmail> emails = getDefaultUser().getEmails().stream()
				.toList();
		
		String signIn = String.format(signInFormat, (current % 2 == 0) ? getDefaultUser().getUsername() : emails.get(0).getEmail(), getDefaultPassword());

		String strJwtToken = getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(signIn))
				.andExpect(MockMvcResultMatchers.status().is(200))
				.andReturn().getResponse().getContentAsString();

		DoubleJWTTokensDTO doublejwtTokens = oMapper.readValue(strJwtToken, DoubleJWTTokensDTO.class);

		getMockMvc().perform(MockMvcRequestBuilders.get("/api/user/i")
				.header("Authorization", "Bearer " + doublejwtTokens.getAccessToken().getToken()))
		.andExpect(MockMvcResultMatchers.status().is(200))
		.andExpect(MockMvcResultMatchers.jsonPath("$.username").value(getDefaultUser().getUsername()));
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
		List<UserEmail> emails = getDefaultUser().getEmails().stream()
				.toList();
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash(emails.get(0).getEmail(), EmailConfirmationScope.LINK);
		
		String signup = String.format(
				signupFormat,
				getDefaultUser().getUsername(),
				emails.get(0).getEmail(),
				getDefaultPassword(),
				codeHash.getCode(),
				codeHash.getHash(),
				codeHash.getTimestamp());
		
		String signUpUri = "/api/auth/signup";

		when(getUserService().save(any())).thenThrow(EntityExistsException.class);
		
		if(info.getCurrentRepetition() % 2 == 1) when(userEmailService.existsByEmail(emails.get(0).getEmail())).thenReturn(false); 

		getMockMvc().perform(MockMvcRequestBuilders.post(signUpUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(signup))
		.andExpect(MockMvcResultMatchers.status().is(409))
		.andExpect(result -> VerifyResolvedException.verify(result, EntityExistsException.class));
	}

	@Test
	void deleteDefaultUser() throws Exception {
		String baseUri = "/api/user/i";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());		
		headers.setIfMatch(Long.toString(getDefaultUser().getVersion()));
		
		when(getUserService().existsByIdAndVersion(getDefaultUser().getId(), getDefaultUser().getVersion())).thenReturn(true);
		
		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri)
				.headers(headers))
		.andExpect(MockMvcResultMatchers.status().isNoContent());
		
		headers.setIfMatch(Long.toString(getDefaultUser().getVersion()/2+1));
		
		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri)
				.headers(headers))
		.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.user.UpdatePasswordOfDefaultUserSource#source")
	void updatePasswordOfDefaultUser(UpdatePasswordOfDefaultUserSource.UpdatePasswordOfDefaultUserData data) throws Exception {
		String updateFormat = """
					{
							"password": "%s"
					}
				""";

		String update = String.format(updateFormat, data.getPassword());
		String updateUri = "/api/user/i";

		getMockMvc().perform(MockMvcRequestBuilders.patch(updateUri)
				.header("Authorization", "Bearer " + getDefaultAccessJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(update))
		.andExpect(MockMvcResultMatchers.status().is(data.getStatus()))
		.andExpect(result -> VerifyResolvedException.verify(result, data.getExpectedException()));
	}
	
	@Test
	void updatePasswordOfDefaultUser() throws Exception {
		String updateFormat = """
					{
							"password": "%s"
					}
				""";

		String update = String.format(updateFormat, UUID.randomUUID().toString());
		String updateUri = "/api/user/i";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
		headers.setIfMatch(Long.toString(getDefaultUser().getVersion()));
				
		when(getUserService().existsByIdAndVersion(getDefaultUser().getId(), getDefaultUser().getVersion())).thenReturn(true);		

		getMockMvc().perform(MockMvcRequestBuilders.patch(updateUri)
				.headers(headers)
				.contentType(MediaType.APPLICATION_JSON)
				.content(update))
		.andExpect(MockMvcResultMatchers.status().isNoContent());
		
		headers.setIfMatch(Long.toString(getDefaultUser().getVersion()+1));
		
		getMockMvc().perform(MockMvcRequestBuilders.patch(updateUri)
				.headers(headers)
				.contentType(MediaType.APPLICATION_JSON)
				.content(update))
		.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}
	
	@Test
	void getI() throws Exception {
		String baseUri = "/api/user/i";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getDefaultAccessJwtToken());
				
		when(getUserService().existsByIdAndVersion(getDefaultUser().getId(), getDefaultUser().getVersion())).thenReturn(true);
		
		getMockMvc().perform(MockMvcRequestBuilders.get(baseUri)
				.headers(headers))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.header().exists("ETag"))
		.andExpect(MockMvcResultMatchers.jsonPath("$.version").value(getDefaultUser().getVersion()));
		
		headers.setIfNoneMatch(Long.toString(getDefaultUser().getVersion()));
		
		getMockMvc().perform(MockMvcRequestBuilders.get(baseUri)
				.headers(headers))
		.andExpect(MockMvcResultMatchers.status().isNotModified());
	}

}
