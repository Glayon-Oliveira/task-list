package com.lmlasmo.tasklist.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.dto.auth.EmailConfirmationCodeHashDTO;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationHashDTO;
import com.lmlasmo.tasklist.exception.InvalidEmailCodeException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class EmailConfirmationService {
	
	@NonNull private EmailService emailService;
		
	private final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();
	private final Base64.Decoder B64URL_DEC = Base64.getUrlDecoder();
	private final String hashBodyFormat = "uuid=%s;code=%s;timestamp=%s;expires=%s";
	private UUID uuid = UUID.randomUUID();
	private byte[] key = generateRandomKey();
	private Duration duration = Duration.ofMinutes(5);
	
	public void update() {
		this.key = generateRandomKey();
		this.uuid = UUID.randomUUID();
	}
	
	public EmailConfirmationHashDTO sendConfirmationEmail(String email) {
		EmailConfirmationCodeHashDTO codeHash = createCodeHash(email);
		
		String emailBody = "Code: " + codeHash.getCode();
		emailService.send(email, "Confirmation code of email", emailBody);
		
		return new EmailConfirmationHashDTO(codeHash.getHash(), codeHash.getTimestamp());
	}
	
	public EmailConfirmationCodeHashDTO createCodeHash(String email) {
		Instant now = Instant.now();
		Instant expires = now.plus(duration);
		
		Double randomCode = new Random().nextDouble(Math.pow(10, 5), Math.pow(10, 6));
		
		String code = Integer.toString(randomCode.intValue());		
		String body = String.format(hashBodyFormat, uuid.toString(), code, now.toString(), expires.toString());
		
		String hash = generateHash(body);		
		
		return new EmailConfirmationCodeHashDTO(hash, now, code);
	}
	
	public void valideCodeHash(EmailConfirmationCodeHashDTO codeHash) {
		Instant expires = codeHash.getTimestamp().plus(duration);
		
		String body = String.format(hashBodyFormat, uuid.toString(), codeHash.getCode(), codeHash.getTimestamp().toString(), expires.toString());
		
		if(!validateHash(body, codeHash.getHash())) throw new InvalidEmailCodeException("Invalid email confirmation code");
	}
	
	private String generateHash(String body) {
		try {
			Mac mac = getMac();
			byte[] result = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
			return B64URL.encodeToString(result);
		}catch(Exception e) {
			throw new RuntimeException("Erro ao criar hash", e);
		}
	}
	
	private boolean validateHash(String body, String hash) {
		try {
			byte[] provided = B64URL_DEC.decode(hash);
			Mac mac = getMac();
			byte[] expected = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
			return MessageDigest.isEqual(expected, provided);
		}catch(Exception e) {
			return false;
		}
	}
	
	private Mac getMac() {
		try {
			String algorithm = "HmacSHA256";
			Mac mac = Mac.getInstance(algorithm);
			mac.init(new SecretKeySpec(key, algorithm));
			return mac;
		} catch (Exception e) {
			throw new RuntimeException("Error creating hash", e);
		}		
	}
	
	private byte[] generateRandomKey() {
		SecureRandom random = new SecureRandom();
		byte[] key = new byte[32];
		random.nextBytes(key);
		return key;
	}
	
}
