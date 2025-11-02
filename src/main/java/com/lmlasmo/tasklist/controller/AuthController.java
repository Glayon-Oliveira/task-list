package com.lmlasmo.tasklist.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.auth.DoubleJWTTokensDTO;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO;
import com.lmlasmo.tasklist.dto.auth.JWTTokenType;
import com.lmlasmo.tasklist.dto.auth.LoginDTO;
import com.lmlasmo.tasklist.dto.auth.TokenDTO;
import com.lmlasmo.tasklist.dto.create.SignupDTO;
import com.lmlasmo.tasklist.exception.InvalidTokenException;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.service.JwtService;
import com.lmlasmo.tasklist.service.UserEmailService;
import com.lmlasmo.tasklist.service.UserService;
import com.nimbusds.jwt.SignedJWT;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@NonNull private UserService userService;	
	@NonNull private JwtService jwtService;
	@NonNull private UserEmailService userEmailService;
	@NonNull private AuthenticationManager manager;
	
	@Value("${app.cookie.secure}")
	private boolean secure;
	
	@PostMapping("/login")
	public ResponseEntity<DoubleJWTTokensDTO> inByJson(@RequestBody @Valid LoginDTO login) throws Exception {		
		Authentication auth = new UsernamePasswordAuthenticationToken(login.getLogin(), login.getPassword());		
		auth = manager.authenticate(auth);
		
		User user = (User) auth.getPrincipal();
		
		String refreshToken = jwtService.generateRefreshToken(user.getId());
		JWTTokenDTO refreshTokenDto = new JWTTokenDTO(refreshToken, JWTTokenType.REFRESH, jwtService.getRefreshTokenDuration().getSeconds());
		JWTTokenDTO accessTokenDto = access(refreshToken, null);
		
		ResponseCookie cookie = ResponseCookie.from("rt", refreshToken)
				.httpOnly(true)
				.secure(secure)
				.path("/api/auth/token/")
				.maxAge(jwtService.getRefreshTokenDuration().getSeconds())
				.sameSite("Strict")
				.build();
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(new DoubleJWTTokensDTO(refreshTokenDto, accessTokenDto));
	}	
	
	@PostMapping("/signup")	
	@ResponseStatus(code = HttpStatus.CREATED)
	public UserDTO upByJson(@RequestBody @Valid SignupDTO signup) {
		if(userEmailService.existsByEmail(signup.getEmail())) throw new EntityExistsException("Email already used");
		
		return userService.save(signup);		
	}	
	
	@PostMapping("/token/refresh")
	public ResponseEntity<DoubleJWTTokensDTO> refresh(@CookieValue(value = "rt", required = false) String refreshToken, @RequestBody(required = false) TokenDTO refreshTokenDto) throws Exception {
		String newRefreshToken = jwtService.regenerateRefreshToken(refreshTokenDto != null ? refreshTokenDto.getToken() : refreshToken);
		
		JWTTokenDTO accessToken = access(newRefreshToken, null);
		JWTTokenDTO newRefreshTokenDto = new JWTTokenDTO(newRefreshToken, JWTTokenType.REFRESH, jwtService.getRefreshTokenDuration().getSeconds());
		
		ResponseCookie cookie = ResponseCookie.from("rt", newRefreshToken)
				.httpOnly(true)
				.secure(true)
				.path("/api/auth/token/")
				.maxAge(jwtService.getRefreshTokenDuration().getSeconds())
				.sameSite("Strict")
				.build();
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(new DoubleJWTTokensDTO(newRefreshTokenDto, accessToken));
	}	
	
	@PostMapping("/token/access")
	public JWTTokenDTO access(@CookieValue(value = "rt", required = false) String refreshToken, @RequestBody(required = false) TokenDTO refreshTokenDto) throws Exception {
		SignedJWT signed = jwtService.validateRefreshToken(refreshTokenDto != null ? refreshTokenDto.getToken() : refreshToken);
		
		int id = jwtService.getSubjectIdOfToken(signed);
		
		try {
			UserDTO user = userService.findById(id);
			String accessToken = jwtService.generateAccessToken(signed, user);
			
			return new JWTTokenDTO(accessToken, JWTTokenType.ACCESS, jwtService.getAccessTokenDuration().getSeconds());
		}catch(EntityNotFoundException e) {
			throw new InvalidTokenException("Invalid subject in token");
		}		
	}

}
