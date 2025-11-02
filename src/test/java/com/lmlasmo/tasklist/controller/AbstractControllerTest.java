package com.lmlasmo.tasklist.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.security.AuthenticatedTool;
import com.lmlasmo.tasklist.security.AuthenticationEntryPointImpl;
import com.lmlasmo.tasklist.security.JWTConf;
import com.lmlasmo.tasklist.security.SecurityConf;
import com.lmlasmo.tasklist.service.JwtService;
import com.lmlasmo.tasklist.service.UserDetailsServiceImpl;
import com.lmlasmo.tasklist.service.UserService;
import com.nimbusds.jwt.SignedJWT;

import lombok.Getter;

@AutoConfigureMockMvc
@Import({SecurityConf.class, JWTConf.class, JwtService.class, AuthenticationEntryPointImpl.class, AuthenticatedTool.class})
public abstract class AbstractControllerTest {

	@Getter
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Getter
	@MockitoBean
	private UserService userService;

	@Getter
	private String defaultAccessJwtToken;
	
	@Getter
	private String defaultRefreshJwtToken;

	@Autowired
	private PasswordEncoder encoder;

	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Getter
	private User defaultUser;

	@Getter
	private final String defaultPassword = "Password - ID = " + UUID.randomUUID();

	@BeforeEach
	protected void setUp() {
		String username = "Username - ID = " + UUID.randomUUID();

		defaultUser = new User(1);
		defaultUser.setEmails(new HashSet<>(Set.of(new UserEmail("test@example.com"))));
		defaultUser.setUsername(username);
		defaultUser.setPassword(encoder.encode(defaultPassword));
		defaultUser.setVersion(new Random().nextLong(Long.MAX_VALUE));
		defaultUser.setCreatedAt(Instant.now());
		defaultUser.setUpdatedAt(defaultUser.getCreatedAt());

		when(userDetailsService.loadUserByUsername(anyString())).thenThrow(UsernameNotFoundException.class);
		when(userDetailsService.loadUserByUsername(eq(username))).thenReturn(defaultUser);
		when(userDetailsService.loadUserByUsername(eq("test@example.com"))).thenReturn(defaultUser);

		when(userService.existsById(anyInt())).thenReturn(true);
		when(userService.findById(anyInt())).thenReturn(new UserDTO(defaultUser));

		defaultRefreshJwtToken = jwtService.generateRefreshToken(defaultUser.getId());
		
		SignedJWT refreshSigned = jwtService.validateRefreshToken(defaultRefreshJwtToken);
		
		defaultAccessJwtToken = jwtService.generateAccessToken(refreshSigned, new UserDTO(defaultUser));
	}

}
