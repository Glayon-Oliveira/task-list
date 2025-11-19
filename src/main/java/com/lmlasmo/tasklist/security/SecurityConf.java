package com.lmlasmo.tasklist.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConf {
	
	@Bean
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, ServerAuthenticationEntryPoint entryPoint,
			Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtConverter) throws Exception {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.cors(cors -> {})
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.authorizeExchange(e -> e.pathMatchers("/api/auth/**").permitAll()
						.anyExchange().authenticated())
				.oauth2ResourceServer(auth2 -> auth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
				.exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
				.build();
	}
	
	@Bean
	public ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService, PasswordEncoder encoder) {
		UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);		
		manager.setPasswordEncoder(encoder);
		
		return manager;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String origins) {
		List<String> originsList = Arrays.stream(origins.split(","))
				.map(String::trim)
				.toList();
		
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(originsList);
		corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		corsConfig.setAllowedHeaders(List.of("*"));
		corsConfig.setAllowCredentials(true);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		
		return source;
	}
	
}
