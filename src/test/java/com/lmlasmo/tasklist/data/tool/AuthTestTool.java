package com.lmlasmo.tasklist.data.tool;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.dto.auth.DoubleJWTTokensDTO;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.service.JWTAuthService;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
@Lazy
public class AuthTestTool {
	
	@Autowired
	private JWTAuthService jwtAuthService;
	
	@Autowired
	private UserTestTool userTestTool;
	
	public void runWithUniqueAuth(Consumer<AuthTest> consumerAuth) {
		userTestTool.runWithUniqueUser(u -> {
			AuthTest authTest = generateAuthTest(u);
			consumerAuth.accept(authTest);
		});
	}
	
	public void runWithDuoAuths(BiConsumer<AuthTest, AuthTest> consumerDuoAuths) {
		userTestTool.runWithDuoUsers((fu, su) -> {
			AuthTest firstAuthTest = generateAuthTest(fu);
			AuthTest secondAuthTest = generateAuthTest(su);
			
			consumerDuoAuths.accept(firstAuthTest, secondAuthTest);
		});
	}
	
	private AuthTest generateAuthTest(User user) {
		Authentication auth = new UsernamePasswordAuthenticationToken(
				user,
				null,
				user.getAuthorities()
				);
		
		DoubleJWTTokensDTO tokens = jwtAuthService.generateDoubleTokenDTO(auth).block();
		AuthTest authTest = new AuthTest(tokens, user);
		
		return authTest;
	}
	
	@Getter
	@AllArgsConstructor
	public static class AuthTest {
		
		private DoubleJWTTokensDTO tokens;
		private User user;
		
	}
	
}
