package com.lmlasmo.tasklist.service;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.annotation.PostConstruct;

@Service
public class JwtService {
	
	@Value("app.jwt.issuer")
	private String issuer;
	
	@Value("app.jwt.key")
	private String key;
	
	private Algorithm algorithm;
	
	@PostConstruct
	private void init() {
		 algorithm = Algorithm.HMAC256(key);
	}
	
	public String gerateToken(int id, String[] roles) {					
		Date issued = new Date();
		Calendar expires = Calendar.getInstance();
		expires.setTime(issued);
		expires.add(Calendar.DAY_OF_YEAR, 30);				
		
		return JWT.create()
				.withIssuer(issuer)
				.withSubject(Integer.toString(id))
				.withArrayClaim("roles", roles)
				.withIssuedAt(new Date())
				.withExpiresAt(expires.getTime())
				.sign(algorithm);		
	}
	
	public void isValid(String token) {		
		JWT.require(algorithm).build().verify(token);		
	}
	
	public int getId(String token) {
		return Integer.parseInt(JWT.require(algorithm).build().verify(token).getSubject());
	}
	
	public String[] getRoles(String token) {
		return JWT.require(algorithm).build().verify(token).getClaim("roles").asArray(String.class);
	}

}
