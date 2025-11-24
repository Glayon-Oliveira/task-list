package com.lmlasmo.tasklist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.auth.DoubleJWTTokensDTO;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO.JWTTokenType;
import com.lmlasmo.tasklist.model.User;
import com.nimbusds.jwt.SignedJWT;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class JWTAuthService {

	@NonNull private JwtService jwtService;
	@NonNull private UserService userService;
	
	@Value("${app.cookie.secure}")
	private boolean secure;
	
	public Mono<JWTTokenDTO> generateAccessTokenDTO(String refreshToken) {
		SignedJWT signed = jwtService.validateRefreshToken(refreshToken);
		int id = jwtService.getSubjectIdOfToken(signed);
		
		return userService.findById(id)
				.switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
				.flatMap(this::generateAccessTokenDTO);
	}
	
	public Mono<JWTTokenDTO> generateAccessTokenDTO(UserDTO user) {
		return userService.lastLoginToNow(user.getId())
				.thenReturn(jwtService.generateAccessToken(user))
				.map(at -> new JWTTokenDTO(at, JWTTokenType.ACCESS, jwtService.getAccessTokenDuration().getSeconds()));
	}
	
	public Mono<JWTTokenDTO> regenerateRefreshTokenDTO(String refreshToken) {
		return Mono.just(jwtService.regenerateRefreshTokenDTO(refreshToken));
	}
	
	public Mono<DoubleJWTTokensDTO> generateDoubleTokenDTO(Authentication auth) {
		User user = (User) auth.getPrincipal();
		
		JWTTokenDTO refreshToken = jwtService.generateRefreshTokenDTO(user.getId());
		
		return generateAccessTokenDTO(new UserDTO(user))
				.map(ajt -> new DoubleJWTTokensDTO(refreshToken, ajt));	
	}
	
	public ResponseCookie createRefreshCookie(JWTTokenDTO refreshToken, String path) {
		return ResponseCookie.from("rt", refreshToken.getToken())
				.httpOnly(true)
				.secure(secure)
				.path(path)
				.maxAge(refreshToken.getDuration())
				.sameSite("Strict")
				.build();
	}
	
}
