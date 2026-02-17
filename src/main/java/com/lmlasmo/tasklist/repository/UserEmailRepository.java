package com.lmlasmo.tasklist.repository;

import java.util.Collection;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.repository.custom.RepositoryCustom;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserEmailRepository extends ReactiveCrudRepository<UserEmail, Integer>, RepositoryCustom {

	public Mono<UserEmail> findByEmail(String email);
	
	public Flux<UserEmail> findByUserId(int userId);
	
	public Flux<UserEmail> findByUserIdAndPrimary(int userId, boolean primary);
	
	public Mono<Boolean> existsByEmail(String email);
	
	public Mono<Boolean> existsByIdAndUserId(int emailId, int userId);

	public Mono<Boolean> existsByEmailAndUserId(String email, int userId);

	public Mono<Boolean> existsByEmailAndStatusIn(String email, Collection<EmailStatusType> status);
	
}
