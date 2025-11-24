package com.lmlasmo.tasklist.service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO;
import com.lmlasmo.tasklist.dto.auth.JWTTokenDTO.JWTTokenType;
import com.lmlasmo.tasklist.exception.ExpiredTokenException;
import com.lmlasmo.tasklist.exception.InvalidTokenException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import lombok.Getter;

@Getter
@Service
public class JwtService {
		
	@Value("${app.jwt.issuer}")
	private String issuer;	
		
	@Value("${app.jwt.refresh-duration}")
	private Duration refreshTokenDuration;
		
	@Value("${app.jwt.access-duration}")
	private Duration accessTokenDuration;
	
	private SignedJWT getSignedJWT(JWTClaimsSet claimsSet, RSAPrivateKey privateKey) {
		try {
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
					.type(JOSEObjectType.JWT)
					.build();
			
			SignedJWT signed = new SignedJWT(header, claimsSet);
			
			RSASSASigner signer = new RSASSASigner(privateKey);
			signed.sign(signer);
			
			return signed;
		}catch(Exception e) {
			throw new RuntimeException("Unknown error", e);
		}		
	}
	
	public SignedJWT validateRefreshToken(String refreshToken) {
		try {
	        SignedJWT signedJWT = SignedJWT.parse(refreshToken);

	        if (!signedJWT.verify(new RSASSAVerifier(Keys.getRefreshPublicKey()))) {
	            throw new InvalidTokenException("Invalid refresh token signature");
	        }

	        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
	        if (claims.getExpirationTime().toInstant().isBefore(Instant.now())) {
	            throw new ExpiredTokenException("Refresh token has expired");
	        }

	        return signedJWT;
		} catch (ParseException | JOSEException | NullPointerException e) {
            throw new InvalidTokenException("Malformed refresh token");
        }
    }
	
	public String generateRefreshToken(int id) {
		return generateRefreshToken(id, null);
	}
	
	public JWTTokenDTO generateRefreshTokenDTO(int id) {
		String token = generateRefreshToken(id, null);
		
		return new JWTTokenDTO(token, JWTTokenType.REFRESH, refreshTokenDuration.getSeconds());
	}
	
	public String generateRefreshToken(int id, Map<String, Object> claims) {
		if(claims == null) claims = Map.of();
		
		Instant now = Instant.now();
		Instant expires = now.plus(refreshTokenDuration);
		
		Builder claimsBuilder = new JWTClaimsSet.Builder();
		
		claims.forEach(claimsBuilder::claim);
				
		claimsBuilder.subject(Integer.toString(id))
			.issuer(issuer)
			.issueTime(Date.from(now))
			.expirationTime(Date.from(expires));
		
		SignedJWT signed = getSignedJWT(claimsBuilder.build(), Keys.getRefreshPrivateKey());
		return signed.serialize();
	}
	
	public JWTTokenDTO generateRefreshTokenDTO(int id, Map<String, Object> claims) {
		String token = generateRefreshToken(id, claims);
		
		return new JWTTokenDTO(token, JWTTokenType.REFRESH, refreshTokenDuration.getSeconds());
	}
	
	public String regenerateRefreshToken(String refreshToken) {
		SignedJWT signed = validateRefreshToken(refreshToken);
		
		try {
			JWTClaimsSet claimsSet = signed.getJWTClaimsSet();
			int sub = getSubjectIdOfToken(signed);
			
			return generateRefreshToken(sub, claimsSet.getClaims());		
		}catch(ParseException e) {
			throw new InvalidTokenException("Invalid token");
		}
	}
	
	public JWTTokenDTO regenerateRefreshTokenDTO(String refreshToken) {
		String token = regenerateRefreshToken(refreshToken);
		
		return new JWTTokenDTO(token, JWTTokenType.REFRESH, refreshTokenDuration.getSeconds());
	}
	
	public String generateAccessToken(UserDTO user) {
		Instant now = Instant.now();
		Instant expires = now.plus(accessTokenDuration);

		Builder claimsSet = new JWTClaimsSet.Builder().subject(Integer.toString(user.getId())).issuer(issuer)
				.issueTime(Date.from(now)).expirationTime(Date.from(expires));

		String role = user.getRole().name();

		claimsSet.claim("roles", role);

		SignedJWT signed = getSignedJWT(claimsSet.build(), Keys.getAccessPrivateKey());
		return signed.serialize();
	}
	
	public JWTTokenDTO generateAccessTokenDTO(UserDTO user) {
		String token = generateAccessToken(user);
		
		return new JWTTokenDTO(token, JWTTokenType.ACCESS, accessTokenDuration.getSeconds());
	}
	
	public int getSubjectIdOfToken(SignedJWT token) {
		try {
			return Integer.parseInt(token.getJWTClaimsSet().getSubject());
		}catch(NumberFormatException e) {
			throw new InvalidTokenException("Invalid subject in token");
		}catch(ParseException e) {
			throw new InvalidTokenException("Malformed refresh token");
		}
	}
	
	public static abstract class Keys {
		private static KeyPair refreshKeys;	
		private static KeyPair accessKeys;
		
		static {
			update();
		}
		
		public static void update() {
			try {
				KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
				
				refreshKeys = generator.generateKeyPair();
				accessKeys = generator.generateKeyPair();			
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("Error creating Key Pair");
			}
		}	
		
		public static RSAPublicKey getRefreshPublicKey() {
			return (RSAPublicKey) refreshKeys.getPublic();
		}
		
		public static RSAPrivateKey getRefreshPrivateKey() {
			return (RSAPrivateKey) refreshKeys.getPrivate();
		}
		
		public static RSAPublicKey getAccessPublicKey() {
			return (RSAPublicKey) accessKeys.getPublic();
		}
		
		public static RSAPrivateKey getAccessPrivateKey() {
			return (RSAPrivateKey) accessKeys.getPrivate();
		}
		
	}

}
