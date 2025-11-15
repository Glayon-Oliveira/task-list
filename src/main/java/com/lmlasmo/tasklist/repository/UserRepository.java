package com.lmlasmo.tasklist.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.custom.UserRepositoryCustom;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Integer>, UserRepositoryCustom {

	public Mono<User> findByUsername(String username);
	
	public Mono<Boolean> existsByUsername(String username);

	public Mono<Boolean> existsByIdAndVersion(int id, long version);
	
}
