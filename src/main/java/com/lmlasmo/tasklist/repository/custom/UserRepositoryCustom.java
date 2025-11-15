package com.lmlasmo.tasklist.repository.custom;

import java.time.Instant;
import java.util.Collection;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.summary.UserSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepositoryCustom extends RepositoryCustom {
	
	public Mono<User> findByEmail(String email);
	
	public Flux<UserSummary.StatusSummary> findStatusSummaryByStatus(UserStatusType status);
	
	public Flux<UserSummary.StatusSummary> findStatusSummaryByStatusAndLastLoginAfter(UserStatusType status, Instant after);

	public Mono<Void> changeStatusByIds(Collection<Integer> ids, UserStatusType status);	
	
}
