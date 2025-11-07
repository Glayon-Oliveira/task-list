package com.lmlasmo.tasklist.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.dto.auth.EmailConfirmationCodeHashDTO;
import com.lmlasmo.tasklist.dto.auth.EmailConfirmationHashDTO;
import com.lmlasmo.tasklist.exception.InvalidEmailCodeException;

import jakarta.persistence.EntityExistsException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class EmailConfirmationService {
	
	@NonNull private EmailService emailService;
	@NonNull private UserEmailService userEmailService;
		
	private final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();
	private final Base64.Decoder B64URL_DEC = Base64.getUrlDecoder();
	private UUID uuid = UUID.randomUUID();
	private byte[] key = generateRandomKey();
	private Duration duration = Duration.ofMinutes(5);
	
	public void update() {
		this.key = generateRandomKey();
		this.uuid = UUID.randomUUID();
	}
	
	public EmailConfirmationHashDTO sendConfirmationEmail(String email, EmailConfirmationScope scope) {
		return sendConfirmationEmail(email, scope, null);
	}
	
	public EmailConfirmationHashDTO sendConfirmationEmail(String email, EmailConfirmationScope scope, Map<String, Object> extra) {		
		EmailConfirmationCodeHashDTO codeHash = createCodeHash(email, scope, extra);
		
		String emailBody = "Code: " + codeHash.getCode();
		emailService.send(email, "Confirmation code of email", emailBody);
		
		return new EmailConfirmationHashDTO(codeHash.getHash(), codeHash.getTimestamp());
	}
	
	public EmailConfirmationCodeHashDTO createCodeHash(String email, EmailConfirmationScope scope) {
		return createCodeHash(email, scope, null);
	}
	
	public EmailConfirmationCodeHashDTO createCodeHash(String email, EmailConfirmationScope scope, Map<String, Object> extra) {
		if(scope.equals(EmailConfirmationScope.LINK) && userEmailService.existsByEmail(email)) {
			throw new EntityExistsException("Email already used");
		}
		
		extra = extra == null ? new TreeMap<>() : new TreeMap<>(extra);
		
		Instant now = Instant.now();
		Instant expires = now.plus(duration);
		
		String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));		
		HashBody body = new HashBody(uuid, email, code, scope, now, expires, extra);
		
		String hash = generateHash(body.toString());		
		
		return new EmailConfirmationCodeHashDTO(hash, now, code);
	}
	
	public void valideCodeHash(EmailConfirmationCodeHashDTO codeHash, String email, EmailConfirmationScope scope) {
		valideCodeHash(codeHash, email, scope, null);
	}
	
	public void valideCodeHash(EmailConfirmationCodeHashDTO codeHash, String email, EmailConfirmationScope scope, Map<String, Object> extra) {
		extra = extra == null ? new TreeMap<>() : new TreeMap<>(extra);
		
		Instant now = Instant.now();
		Instant expires = codeHash.getTimestamp().plus(duration);
		
		if(now.isAfter(expires)) throw new InvalidEmailCodeException("Email confirmation code has expired");
		
		HashBody body = new HashBody(uuid, email, codeHash.getCode(), scope, codeHash.getTimestamp(), expires, extra);
		
		if(!validateHash(body.toString(), codeHash.getHash())) throw new InvalidEmailCodeException("Invalid email confirmation code");
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
	
	private record HashBody(
			UUID uuid,
			String email,
			String code,			
			EmailConfirmationScope scope,
			Instant timestamp,
			Instant expires,
			Map<String, Object> extra) {}
	
	public enum EmailConfirmationScope {
		LINK,
		RECOVERY,
	}
	
}
