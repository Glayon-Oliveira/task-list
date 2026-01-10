package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lmlasmo.tasklist.doc.controller.auth.AccessApiDoc;
import com.lmlasmo.tasklist.doc.controller.auth.EmailConfirmationApiDoc;
import com.lmlasmo.tasklist.doc.controller.auth.LoginApiDoc;
import com.lmlasmo.tasklist.doc.controller.auth.PasswordRecoveryApiDoc;
import com.lmlasmo.tasklist.doc.controller.auth.RefreshApiDoc;
import com.lmlasmo.tasklist.doc.controller.auth.SignupApiDoc;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.auth.DoubleJWTTokensDTO;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationHashDTO;
import com.lmlasmo.tasklist.dto.auth.EmailWithScope;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO;
import com.lmlasmo.tasklist.dto.auth.LoginDTO;
import com.lmlasmo.tasklist.dto.auth.PasswordRecoveryDTO;
import com.lmlasmo.tasklist.dto.auth.SignupDTO;
import com.lmlasmo.tasklist.dto.auth.TokenDTO;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;
import com.lmlasmo.tasklist.service.JWTAuthService;
import com.lmlasmo.tasklist.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private UserService userService;	
	private JWTAuthService authJWTService;
	private ReactiveAuthenticationManager manager;
	private EmailConfirmationService confirmationService;
	
	@LoginApiDoc
	@PostMapping(path = "/login", headers = "X-RefreshToken-Provider", produces = MediaType.APPLICATION_JSON_VALUE)	
	public Mono<DoubleJWTTokensDTO> loginWithHeader(@RequestBody @Valid LoginDTO login) {
		Authentication auth = new UsernamePasswordAuthenticationToken(login.getLogin(), login.getPassword());
		
		return manager.authenticate(auth)
				.flatMap(authJWTService::generateDoubleTokenDTO);
	}
	
	@LoginApiDoc
	@PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)	
	public Mono<ResponseEntity<JWTTokenDTO>> login(@RequestBody @Valid LoginDTO login) {
		return loginWithHeader(login)
					.map(djt -> {
						ResponseCookie cookie = authJWTService.createRefreshCookie(djt.getRefreshToken(), "/api/auth/token");
						
						return ResponseEntity.ok()
								.header(HttpHeaders.SET_COOKIE, cookie.toString())
								.body(djt.getAccessToken());
					});
	}
	
	@SignupApiDoc
	@PostMapping(path = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(code = HttpStatus.CREATED)
	public Mono<UserDTO> upByJson(@RequestBody @Valid SignupDTO signup) {		
		return confirmationService.valideCodeHash(signup.getConfirmation(), signup.getEmail(), EmailConfirmationScope.LINK)
				.then(userService.save(signup));
	}	
	
	@RefreshApiDoc
	@PostMapping(path = "/token/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<DoubleJWTTokensDTO> refresh(@RequestBody @Valid TokenDTO refreshTokenDto) {
		return authJWTService.regenerateRefreshTokenDTO(refreshTokenDto.getToken())
				.flatMap(rjt -> authJWTService.generateAccessTokenDTO(rjt.getToken())
						.map(ajt -> new DoubleJWTTokensDTO(rjt, ajt)));
	}
	
	@RefreshApiDoc
	@PostMapping(path = "/token/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<JWTTokenDTO>> refresh(@CookieValue(value = "rt") String refreshToken) {
		return refresh(new TokenDTO(refreshToken))
				.map(djt -> {
					ResponseCookie cookie = authJWTService.createRefreshCookie(djt.getRefreshToken(), "/api/auth/token");
					
					return ResponseEntity.ok()
							.header(HttpHeaders.SET_COOKIE, cookie.toString())
							.body(djt.getAccessToken());
				});
	}
	
	@AccessApiDoc
	@PostMapping(path = "/token/access", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<JWTTokenDTO> access(@RequestBody @Valid TokenDTO refreshTokenDto) {		
		return authJWTService.generateAccessTokenDTO(refreshTokenDto.getToken());
	}
	
	@AccessApiDoc
	@PostMapping(path = "/token/access", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<JWTTokenDTO> access(@CookieValue(value = "rt") String refreshToken) {		
		return authJWTService.generateAccessTokenDTO(refreshToken);
	}
	
	@EmailConfirmationApiDoc
	@PostMapping(path = "/email/confirmation", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<EmailConfirmationHashDTO> confirmEmail(@RequestBody @Valid EmailWithScope email) {
		return confirmationService.sendConfirmationEmail(email.getEmail(), email.getScope());
	}
	
	@PasswordRecoveryApiDoc
	@PatchMapping("/recover/password")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> recoverPassword(@RequestBody @Valid PasswordRecoveryDTO passwordRecovery) {		
		return confirmationService.valideCodeHash(passwordRecovery.getConfirmation(), passwordRecovery.getEmail(), EmailConfirmationScope.RECOVERY)
				.then(userService.updatePassword(passwordRecovery));
	}

}
