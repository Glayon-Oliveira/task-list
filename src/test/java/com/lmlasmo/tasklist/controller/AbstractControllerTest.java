package com.lmlasmo.tasklist.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.model.RoleType;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.security.AuthenticationEntryPointImpl;
import com.lmlasmo.tasklist.security.JwtAuthenticationFilter;
import com.lmlasmo.tasklist.security.SecurityConf;
import com.lmlasmo.tasklist.service.JwtService;
import com.lmlasmo.tasklist.service.UserDetailsServiceImpl;
import com.lmlasmo.tasklist.service.UserService;

import lombok.Getter;

@AutoConfigureMockMvc
@Import({SecurityConf.class, JwtAuthenticationFilter.class, JwtService.class, AuthenticationEntryPointImpl.class})
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
	private String defaultJwtToken;

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
		defaultUser.setUsername(username);
		defaultUser.setPassword(encoder.encode(defaultPassword));
		defaultUser.setVersion(new Random().nextLong(Long.MAX_VALUE));
		defaultUser.setCreatedAt(Instant.now());
		defaultUser.setUpdatedAt(defaultUser.getCreatedAt());

		when(userDetailsService.loadUserByUsername(anyString())).thenThrow(UsernameNotFoundException.class);
		when(userDetailsService.loadUserByUsername(eq(username))).thenReturn(defaultUser);

		when(userService.existsById(anyInt())).thenReturn(true);
		when(userService.findById(anyInt())).thenReturn(new UserDTO(defaultUser));

		defaultJwtToken = jwtService.gerateToken(defaultUser.getId(), Arrays.array(RoleType.COMUM.name()));
	}

}
