package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lmlasmo.tasklist.dto.auth.EmailConfirmationCodeHashDTO;
import com.lmlasmo.tasklist.exception.InvalidEmailCodeException;
import com.lmlasmo.tasklist.service.EmailConfirmationService.EmailConfirmationScope;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class EmailConfirmationServiceTest {

	@Mock
	private EmailService emailService;
	
	@Mock
	private UserEmailService userEmailService;
	
	@InjectMocks
	private EmailConfirmationService confirmationService;
	
	@Test
	public void sendConfirmationEmailAndValidation() {
		String email = "test@example.com";
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash(email, EmailConfirmationScope.LINK);
		String code = codeHash.getCode();
		String hash = codeHash.getHash();
		Instant timestamp = codeHash.getTimestamp();
		
		EmailConfirmationCodeHashDTO confirmationCodeHashDto = new EmailConfirmationCodeHashDTO(hash, timestamp, code);
		
		assertDoesNotThrow(() -> confirmationService.valideCodeHash(confirmationCodeHashDto, EmailConfirmationScope.LINK));
	}
	
	@RepeatedTest(3)
	public void sendConfirmationEmailAndFailureValidation(RepetitionInfo info) {
		String email = "test@example.com";
		int current = info.getCurrentRepetition();
		
		EmailConfirmationCodeHashDTO codeHash = confirmationService.createCodeHash(email, EmailConfirmationScope.LINK);
		String code = current == 1 ? "135256" :codeHash.getCode();		
		Instant timestamp = current == 2 ? Instant.now() : codeHash.getTimestamp();
		String hash = current == 3 ? "nhsdhfiosjsdfojndndoewi" : codeHash.getHash();
		
		EmailConfirmationCodeHashDTO confirmationCodeHashDto = new EmailConfirmationCodeHashDTO(hash, timestamp, code);
		
		assertThrows(InvalidEmailCodeException.class, () -> confirmationService.valideCodeHash(confirmationCodeHashDto, EmailConfirmationScope.LINK));
	}
	
	@Test
	public void update() {
		confirmationService.update();
	}
	
}
