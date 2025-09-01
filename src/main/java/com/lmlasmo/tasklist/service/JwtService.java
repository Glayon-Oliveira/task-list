package com.lmlasmo.tasklist.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.annotation.PostConstruct;

@Service
public class JwtService {
	
	@Value("${app.jwt.issuer}")
	private String issuer;
	
	@Value("${app.jwt.key}")
	private String key;
	
	@Value("${app.jwt.duration}")
	private Duration duration;
	
	private Algorithm algorithm;
	
	@PostConstruct
	private void init() {
		algorithm = Algorithm.HMAC256(key);
	}
	
	public String gerateToken(int id, String[] roles) {
		Instant issued = Instant.now();
		Instant expires = issued.plus(duration);
		
		return JWT.create()
				.withIssuer(issuer)
				.withSubject(Integer.toString(id))
				.withArrayClaim("roles", roles)
				.withIssuedAt(Date.from(issued))
				.withExpiresAt(Date.from(expires))
				.sign(algorithm);		
	}
	
	public void isValid(String token) {		
		JWT.require(algorithm).build().verify(token);
	}
	
	public Integer getId(String token) {
		try {
			return Integer.parseInt(JWT.require(algorithm).build().verify(token).getSubject());
		}catch(Exception e) {
			return null;
		}		
	}
	
	public String[] getRoles(String token) {
		return JWT.require(algorithm).build().verify(token).getClaim("roles").asArray(String.class);
	}

}
