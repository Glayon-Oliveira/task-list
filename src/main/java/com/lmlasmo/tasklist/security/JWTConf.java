package com.lmlasmo.tasklist.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.lmlasmo.tasklist.service.JwtService;
import com.lmlasmo.tasklist.service.JwtService.Keys;
import com.lmlasmo.tasklist.service.UserService;

@Configuration
public class JWTConf {

	@Bean
	public JwtDecoder jwtDecoder(OAuth2TokenValidator<Jwt> authTokenValidator) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(Keys.getAccessPublicKey()).build();
		
		decoder.setJwtValidator(authTokenValidator);
		return decoder;
	}
	
	@Bean
	public OAuth2TokenValidator<Jwt> authTokenValidator(JwtService jwtService, UserService userService) {
		OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(jwtService.getIssuer());
		
		OAuth2TokenValidator<Jwt> existsValidator = jwt -> {
			try {
				int id = Integer.parseInt(jwt.getSubject());
				
				if(!userService.existsById(id).block()) return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid user"));
				
				return OAuth2TokenValidatorResult.success();
			}catch(Exception e) {}
			
			return OAuth2TokenValidatorResult.failure(new OAuth2Error("Invalid token"));
		};
		
		OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
				issuerValidator,
				existsValidator);
		
		return validator;
	}
	
	@Bean
	public JwtAuthenticationConverter authenticationConverter() {
		JwtGrantedAuthoritiesConverter authorityConverter = new JwtGrantedAuthoritiesConverter();
		authorityConverter.setAuthorityPrefix("ROLE_");
		authorityConverter.setAuthoritiesClaimName("roles");
		
		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
		authenticationConverter.setJwtGrantedAuthoritiesConverter(authorityConverter);
		authenticationConverter.setPrincipalClaimName("sub");
		
		return authenticationConverter;
	}
	
}
