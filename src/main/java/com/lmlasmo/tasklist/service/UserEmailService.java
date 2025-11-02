package com.lmlasmo.tasklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.UserEmailDTO;
import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.repository.UserEmailRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserEmailService {

	private UserEmailRepository emailRepository;
	
	public UserEmailDTO save(String email, int userId) {
		if(emailRepository.existsByEmail(email)) throw new EntityExistsException("Email already used");
		
		UserEmail userEmail = new UserEmail(email);
		userEmail.setUser(new User(userId));
		
		return new UserEmailDTO(emailRepository.save(userEmail));
	}
	
	public UserEmailDTO changePrimaryEmail(int emailId, int userId) {
		List<UserEmail> userEmails = emailRepository.findByUserId(userId);
		
		UserEmail targetEmail = userEmails.stream()
				.filter(e -> e.getId() == emailId)
				.findFirst()
				.orElseThrow(() -> new EntityNotFoundException("Email not found"));
		
		UserEmail primaryEmail = userEmails.stream()
				.filter(e -> e.isPrimary())
				.findFirst()
				.orElseGet(() -> null);
		
		if(primaryEmail != null) {
			primaryEmail.setPrimary(false);
			emailRepository.save(primaryEmail);
		}
		
		targetEmail.setPrimary(true);
		return new UserEmailDTO(emailRepository.save(targetEmail));
	}
	
	public UserEmailDTO changeEmailStatus(String email, EmailStatusType status) {
		UserEmail userEmail = emailRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Email not found"));
		
		userEmail.setStatus(status);
		
		return new UserEmailDTO(emailRepository.save(userEmail));
	}

	public boolean existsByEmail(String email) {
		return emailRepository.existsByEmail(email);
	}
	
}
