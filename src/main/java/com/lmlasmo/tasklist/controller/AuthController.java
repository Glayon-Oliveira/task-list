package com.lmlasmo.tasklist.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
	
	@PostMapping("/login")
	public Mono<ResponseEntity<DoubleJWTTokensDTO>> inByJson(@RequestBody @Valid LoginDTO login) throws Exception {		
		Authentication auth = new UsernamePasswordAuthenticationToken(login.getLogin(), login.getPassword());
		
		return manager.authenticate(auth)
				.flatMap(authJWTService::generateDoubleTokenDTO)
				.flatMap(djt -> {
					ResponseCookie cookie = authJWTService.createRefreshCookie(djt.getRefreshToken(), "/api/auth/token/");
					
					return Mono.just(ResponseEntity.ok()
							.header(HttpHeaders.SET_COOKIE, cookie.toString())
							.body(djt));
				});
	}	
	
	@PostMapping("/signup")	
	@ResponseStatus(code = HttpStatus.CREATED)
	public UserDTO upByJson(@RequestBody @Valid SignupDTO signup) {		
		confirmationService.valideCodeHash(signup.getConfirmation(), signup.getEmail(), EmailConfirmationScope.LINK);
		
		return userService.save(signup).block();
	}	
	
	@PostMapping("/token/refresh")
	public Mono<ResponseEntity<DoubleJWTTokensDTO>> refresh(@CookieValue(value = "rt", required = false) String refreshToken, @RequestBody(required = false) @Valid TokenDTO refreshTokenDto) throws Exception {
		
		return authJWTService.regenerateRefreshTokenDTO(refreshTokenDto != null ? refreshTokenDto.getToken() : refreshToken)
				.flatMap(rjt -> {
					return authJWTService.generateAccessTokenDTO(rjt.getToken())
							.map(ajt -> new DoubleJWTTokensDTO(rjt, ajt));
				})
				.flatMap(djt -> {
					ResponseCookie cookie = authJWTService.createRefreshCookie(djt.getRefreshToken(), "/api/auth/token/");
					
					return Mono.just(ResponseEntity.ok()
							.header(HttpHeaders.SET_COOKIE, cookie.toString())
							.body(djt));
				});
	}	
	
	@PostMapping("/token/access")
	public Mono<JWTTokenDTO> access(@CookieValue(value = "rt", required = false) String refreshToken, @RequestBody(required = false) @Valid TokenDTO refreshTokenDto) {
		return authJWTService.generateAccessTokenDTO(refreshTokenDto != null ? refreshTokenDto.getToken() : refreshToken);
	}
	
	@PostMapping("/email/confirmation")
	public Mono<EmailConfirmationHashDTO> confirmEmail(@RequestBody @Valid EmailWithScope email) {
		return confirmationService.sendConfirmationEmail(email.getEmail(), email.getScope());
	}
	
	@PatchMapping("/recover/password")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Mono<Void> recoverPassword(@RequestBody @Valid PasswordRecoveryDTO passwordRecovery) {		
		return confirmationService.valideCodeHash(passwordRecovery.getConfirmation(), passwordRecovery.getEmail(), EmailConfirmationScope.RECOVERY)
				.then(userService.updatePassword(passwordRecovery));
	}

}
