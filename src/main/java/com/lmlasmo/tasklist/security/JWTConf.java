package com.lmlasmo.tasklist.security;

import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.lmlasmo.tasklist.service.JwtService.Keys;
import com.lmlasmo.tasklist.service.UserService;

import reactor.core.publisher.Mono;

@Configuration
public class JWTConf {
	
	@Bean
	public ReactiveJwtDecoder reactiveJwtDecoder() {
		return NimbusReactiveJwtDecoder
				.withPublicKey(Keys.getAccessPublicKey())
				.build();
	}
	
	@Bean
	public Converter<Jwt, Mono<AbstractAuthenticationToken>> reactiveAauthenticationConverter2(UserService userService) {
		JwtGrantedAuthoritiesConverter authorityConverter = new JwtGrantedAuthoritiesConverter();
		authorityConverter.setAuthoritiesClaimName("roles");
		
		return jwt -> {
			 Collection<GrantedAuthority> authorities = authorityConverter.convert(jwt);
			 
			 Mono<Integer> userId = Mono.fromCallable(() -> Integer.parseInt(jwt.getSubject()))
		                .onErrorResume(e -> Mono.error(new BadCredentialsException("Invalid token")));
			 
			 return userId.flatMap(id -> userService.existsById(id)
					 				.flatMap(e -> e
					 						? Mono.just(new UsernamePasswordAuthenticationToken(id, null, authorities))
					 						: Mono.error(new BadCredentialsException("Invalid token"))
					 				));
		};
	}
	
}
