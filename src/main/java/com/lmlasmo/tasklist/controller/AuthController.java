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
import com.lmlasmo.tasklist.dto.auth.JWTTokenType;
import com.lmlasmo.tasklist.dto.auth.LoginDTO;
import com.lmlasmo.tasklist.dto.auth.PasswordRecoveryDTO;
import com.lmlasmo.tasklist.dto.auth.SignupDTO;
import com.lmlasmo.tasklist.dto.auth.TokenDTO;
import com.lmlasmo.tasklist.exception.InvalidTokenException;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.service.EmailConfirmationService;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;
import com.lmlasmo.tasklist.service.JwtService;
import com.lmlasmo.tasklist.service.UserEmailService;
import com.lmlasmo.tasklist.service.UserService;
import com.nimbusds.jwt.SignedJWT;

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
	@NonNull private EmailConfirmationService confirmationService;
	
	@Value("${app.cookie.secure}")
	private boolean secure;
	
	@PostMapping("/login")
	public ResponseEntity<DoubleJWTTokensDTO> inByJson(@RequestBody @Valid LoginDTO login) throws Exception {		
		Authentication auth = new UsernamePasswordAuthenticationToken(login.getLogin(), login.getPassword());		
		auth = manager.authenticate(auth);
		
		User user = (User) auth.getPrincipal();
		userService.lastLoginToNow(user.getId());
		
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
		confirmationService.valideCodeHash(signup.getConfirmation(), signup.getEmail(), EmailConfirmationScope.LINK);
		
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
			
			userService.lastLoginToNow(id);
			
			return new JWTTokenDTO(accessToken, JWTTokenType.ACCESS, jwtService.getAccessTokenDuration().getSeconds());
		}catch(EntityNotFoundException e) {
			throw new InvalidTokenException("Invalid subject in token");
		}		
	}
	
	@PostMapping("/email/confirmation")
	public EmailConfirmationHashDTO confirmEmail(@RequestBody @Valid EmailWithScope email) {
		return confirmationService.sendConfirmationEmail(email.getEmail(), email.getScope());
	}
	
	@PatchMapping("/recover/password")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public Void recoverPassword(@RequestBody @Valid PasswordRecoveryDTO passwordRecovery) {
		confirmationService.valideCodeHash(passwordRecovery.getConfirmation(), passwordRecovery.getEmail(), EmailConfirmationScope.RECOVERY);
		userService.updatePassword(passwordRecovery);
		return null;
	}

}
