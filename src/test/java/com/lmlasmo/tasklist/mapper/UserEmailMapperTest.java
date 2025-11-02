package com.lmlasmo.tasklist.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import com.lmlasmo.tasklist.dto.UserEmailDTO;
import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.model.UserEmail;

public class UserEmailMapperTest {

	@RepeatedTest(2)
	public void userEmailToDto(RepetitionInfo info) {
		UserEmail userEmail = new UserEmail("test@example.com");
		userEmail.setPrimary(info.getCurrentRepetition() % 2 == 0);
		userEmail.setStatus(info.getCurrentRepetition() % 2 == 0 ? EmailStatusType.ACTIVE : EmailStatusType.INACTIVE);
		
		UserEmailDTO userEmailDto = new UserEmailDTO(userEmail);
		
		assertTrue(userEmailDto.getEmail().equals(userEmail.getEmail()));
		assertTrue(userEmailDto.getStatus().equals(userEmail.getStatus()));
		assertTrue(userEmailDto.isPrimary() == (userEmail.isPrimary()));
	}
	
}
