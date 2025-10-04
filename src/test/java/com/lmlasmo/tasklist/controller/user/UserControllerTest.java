package com.lmlasmo.tasklist.controller.user;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.SignController;
import com.lmlasmo.tasklist.controller.UserController;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO;
import com.lmlasmo.tasklist.dto.create.SignupDTO;
import com.lmlasmo.tasklist.param.user.SignInAndUpSource;
import com.lmlasmo.tasklist.param.user.UpdatePasswordOfDefaultUserSource;
import com.lmlasmo.tasklist.util.VerifyResolvedException;

import jakarta.persistence.EntityExistsException;

@WebMvcTest(controllers = {SignController.class, UserController.class})
@TestInstance(Lifecycle.PER_CLASS)
public class UserControllerTest extends AbstractControllerTest {

	@Autowired
	private ObjectMapper oMapper;

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.user.SignInAndUpSource#source")
	void signUp(SignInAndUpSource.SignInAndUpData data) throws Exception {
		String createFormat = """
					{
							"username": "%s",
							"password": "%s"
					}
				""";

		String create = String.format(createFormat, data.getUsername(), data.getPassword());
		String signUpUri = "/api/sign/up";

		when(getUserService().save(any(SignupDTO.class))).thenReturn(new UserDTO(getDefaultUser()));

		ResultActions resultActions = getMockMvc().perform(MockMvcRequestBuilders.post(signUpUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(create))
				.andExpect(MockMvcResultMatchers.status().is(data.getStatus()))
				.andExpect(result -> VerifyResolvedException.verify(result, data.getExpectedException()));

		assumeTrue(data.getStatus() >= 200 && data.getStatus() < 300);

		resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.username").value(getDefaultUser().getUsername()));
	}

	@ParameterizedTest
	@MethodSource("com.lmlasmo.tasklist.param.user.SignInAndUpSource#source")
	void signIn(SignInAndUpSource.SignInAndUpData data) throws Exception {
		String baseUri = "/api/sign/in";
		String signInFormat = """
					{
							"username": "%s",
							"password": "%s"
					}
				""";

		assumeTrue(data.getStatus() < 200 || data.getStatus() >= 300);

		String signIn = String.format(signInFormat, data.getUsername(), data.getPassword());

		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(signIn))
		.andExpect(MockMvcResultMatchers.status().is(data.getStatus()))
		.andExpect(result -> VerifyResolvedException.verify(result, data.getExpectedException()));
	}

	@Test
	void successSignIn() throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/sign/in";
		String signInFormat = """
					{
							"username": "%s",
							"password": "%s"
					}
				""";

		String signIn = String.format(signInFormat, getDefaultUser().getUsername(), getDefaultPassword());

		String strJwtToken = getMockMvc().perform(MockMvcRequestBuilders.post(baseUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(signIn))
				.andExpect(MockMvcResultMatchers.status().is(200))
				.andReturn().getResponse().getContentAsString();

		JWTTokenDTO jwtToken = oMapper.readValue(strJwtToken, JWTTokenDTO.class);

		getMockMvc().perform(MockMvcRequestBuilders.get("/api/user/i")
				.header("Authorization", "Bearer " + jwtToken.getToken()))
		.andExpect(MockMvcResultMatchers.status().is(200))
		.andExpect(MockMvcResultMatchers.jsonPath("$.username").value(getDefaultUser().getUsername()));
	}

	@Test
	void signUpConflitedWithDefaultUser() throws Exception {
		String signupFormat = """
					{
							"username": "%s",
							"password": "%s"
					}
				""";

		String signup = String.format(signupFormat, getDefaultUser().getUsername(), getDefaultPassword());
		String signUpUri = "/api/sign/up";

		when(getUserService().save(any())).thenThrow(EntityExistsException.class);

		getMockMvc().perform(MockMvcRequestBuilders.post(signUpUri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(signup))
		.andExpect(MockMvcResultMatchers.status().is(409))
		.andExpect(result -> VerifyResolvedException.verify(result, EntityExistsException.class));
	}

	@Test
	void deleteDefaultUser() throws Exception {
		String baseUri = "/api/user/";

		getMockMvc().perform(MockMvcRequestBuilders.delete(baseUri)
				.header("Authorization", "Bearer " + getDefaultJwtToken()))
		.andExpect(MockMvcResultMatchers.status().is(204));
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
		String updateUri = "/api/user/";

		getMockMvc().perform(MockMvcRequestBuilders.put(updateUri)
				.header("Authorization", "Bearer " + getDefaultJwtToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(update))
		.andExpect(MockMvcResultMatchers.status().is(data.getStatus()))
		.andExpect(result -> VerifyResolvedException.verify(result, data.getExpectedException()));
	}

}
