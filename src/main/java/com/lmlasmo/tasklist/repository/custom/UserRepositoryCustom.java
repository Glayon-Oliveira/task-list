package com.lmlasmo.tasklist.repository.custom;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.summary.UserSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepositoryCustom extends RepositoryCustom {
	
	public Mono<User> findByEmail(String email);
	
	public Mono<UserSummary> findSummaryByEmail(String email, Set<String> includedFields);
	
	public Flux<UserSummary> findSummaryByStatus(UserStatusType status, Set<String> includedFields);
	
	public Flux<UserSummary> findSummaryByStatusAndLastLoginAfter(UserStatusType status, Instant after, Set<String> includedFields);

	public Mono<Void> changeStatusByIds(Collection<Integer> ids, UserStatusType status);
	
}
