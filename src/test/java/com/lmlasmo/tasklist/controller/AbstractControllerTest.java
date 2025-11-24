package com.lmlasmo.tasklist.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.reactive.config.EnableWebFlux;

import com.lmlasmo.tasklist.controller.AbstractControllerTest.TestWebFluxConfig;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.security.AuthenticatedResourceAccess;
import com.lmlasmo.tasklist.security.AuthenticationEntryPointImpl;
import com.lmlasmo.tasklist.security.JWTConf;
import com.lmlasmo.tasklist.security.SecurityConf;
import com.lmlasmo.tasklist.service.JWTAuthService;
import com.lmlasmo.tasklist.service.JwtService;
import com.lmlasmo.tasklist.service.UserDetailsServiceImpl;
import com.lmlasmo.tasklist.service.UserService;

import lombok.Getter;
import reactor.core.publisher.Mono;

@AutoConfigureWebTestClient
@EnableWebFlux
@Import({SecurityConf.class,
		JWTConf.class, 
		JwtService.class,
		JWTAuthService.class,
		AuthenticationEntryPointImpl.class, 
		AuthenticatedResourceAccess.class,
		TestWebFluxConfig.class})
public abstract class AbstractControllerTest {

	@Getter
	@Autowired
	private WebTestClient webTestClient;

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
		defaultUser.setUsername(username);
		defaultUser.setPassword(encoder.encode(defaultPassword));
		defaultUser.setVersion(new Random().nextLong(Long.MAX_VALUE));
		defaultUser.setCreatedAt(Instant.now());
		defaultUser.setUpdatedAt(defaultUser.getCreatedAt());

		when(userDetailsService.findByUsername(anyString())).thenThrow(UsernameNotFoundException.class);
		when(userDetailsService.findByUsername(eq(username))).thenReturn(Mono.just(defaultUser));
		when(userDetailsService.findByUsername(eq("test@example.com"))).thenReturn(Mono.just(defaultUser));

		when(userService.existsById(anyInt())).thenReturn(Mono.just(true));
		when(userService.findById(anyInt())).thenReturn(Mono.just(new UserDTO(defaultUser)));

		defaultRefreshJwtToken = jwtService.generateRefreshToken(defaultUser.getId());
		defaultAccessJwtToken = jwtService.generateAccessToken(new UserDTO(defaultUser));
	}
	
	@TestConfiguration
	public static class TestWebFluxConfig {

	    @Bean
	    public ServerWebExchangeContextFilter exchangeFilter() {
	        return new ServerWebExchangeContextFilter();
	    }
	}


}
