package com.lmlasmo.tasklist.controller.auth;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.lmlasmo.tasklist.controller.AbstractControllerTest;
import com.lmlasmo.tasklist.controller.AuthController;
import com.lmlasmo.tasklist.dto.auth.JWTTokenType;
import com.lmlasmo.tasklist.service.UserEmailService;

import jakarta.servlet.http.Cookie;

@WebMvcTest(controllers = {AuthController.class})
@TestInstance(Lifecycle.PER_CLASS)
public class TokenJwtTest extends AbstractControllerTest {
	
	@MockitoBean
	private UserEmailService userEmailService;
	
	@Test
	void successRefreshToken() throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token";
		
		Cookie cookie = new Cookie("rt", getDefaultRefreshJwtToken());
		
		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri + "/refresh")
				.cookie(cookie))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken.token").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken.type").value(JWTTokenType.REFRESH.name()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken.duration").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.token").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.type").value(JWTTokenType.ACCESS.name()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.duration").exists());
		
		String token = """
				{
				"token": "%s"
				}
				""";
		
		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri + "/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(String.format(token, getDefaultRefreshJwtToken())))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken.token").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken.type").value(JWTTokenType.REFRESH.name()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken.duration").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.token").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.type").value(JWTTokenType.ACCESS.name()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.duration").exists());
	}
	
	@Test
	void successAccessToken() throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token";
				
		Cookie cookie = new Cookie("rt", getDefaultRefreshJwtToken());
		
		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri + "/access")
				.cookie(cookie))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.type").value(JWTTokenType.ACCESS.name()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.duration").exists());
		
		String token = """
				{
				"token": "%s"
				}
				""";
		
		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri + "/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(String.format(token, getDefaultRefreshJwtToken())))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.token").exists())
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.type").value(JWTTokenType.ACCESS.name()))
		.andExpect(MockMvcResultMatchers.jsonPath("$.accessToken.duration").exists());
	}
	
	@Test
	void failureRefreshToken() throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token";
		
		Cookie cookie = new Cookie("rt", UUID.randomUUID().toString());
		
		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri + "/refresh")
				.cookie(cookie))
		.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		
		String token = """
				"token": "%s"
				""";
		
		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri + "/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(String.format(token, UUID.randomUUID().toString())))
		.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}
	
	@Test
	void failureAccessToken() throws UnsupportedEncodingException, Exception {
		String baseUri = "/api/auth/token";
		
		Cookie cookie = new Cookie("rt", UUID.randomUUID().toString());
		
		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri + "/access")
				.cookie(cookie))
		.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		
		String token = """
				"token": "%s"
				""";
		
		getMockMvc().perform(MockMvcRequestBuilders.post(baseUri + "/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(String.format(token, UUID.randomUUID().toString())))
		.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}
	
}
