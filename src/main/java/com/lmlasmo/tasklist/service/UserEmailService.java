package com.lmlasmo.tasklist.service;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.UserEmailDTO;
import com.lmlasmo.tasklist.exception.ResourceAlreadyExistsException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.repository.UserEmailRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@AllArgsConstructor
@Service
public class UserEmailService {

	private UserEmailRepository emailRepository;
	
	public Mono<UserEmailDTO> save(String email, int userId) {
		return emailRepository.existsByEmail(email)
				.filter(e -> !e)
				.switchIfEmpty(Mono.error(new ResourceAlreadyExistsException("Email already used")))
				.thenReturn(new UserEmail(email, userId))
				.flatMap(emailRepository::save)
				.map(UserEmailDTO::new);
	}
	
	public Mono<UserEmailDTO> changePrimaryEmail(int emailId, int userId) {
		Mono<UserEmail> targetEmail =  emailRepository.findById(emailId)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Email not found")))
				.doOnNext(ue -> ue.setPrimary(true))
				.flatMap(emailRepository::save)
				.switchIfEmpty(Mono.error(new OptimisticLockingFailureException("Email was updated by another transaction")));
		
		Mono<UserEmail> primaryEmail = emailRepository.findByUserIdAndPrimary(userId, true)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Email primary not found")))
				.doOnNext(ue -> ue.setPrimary(false))
				.flatMap(emailRepository::save)
				.switchIfEmpty(Mono.error(new OptimisticLockingFailureException("Email was updated by another transaction")));
		
		return Mono.zip(targetEmail, primaryEmail)
				.map(Tuple2::getT1)
				.map(UserEmailDTO::new)
				.as(m -> emailRepository.getOperator().transactional(m));
	}
	
	public Mono<UserEmailDTO> changeEmailStatus(String email, EmailStatusType status) {
		return emailRepository.findByEmail(email)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("Email not found")))
				.doOnNext(ue -> ue.setStatus(status))
				.flatMap(emailRepository::save)
				.map(UserEmailDTO::new);
	}

	public Mono<Boolean> existsByEmail(String email) {
		return emailRepository.existsByEmail(email);
	}
	
}
