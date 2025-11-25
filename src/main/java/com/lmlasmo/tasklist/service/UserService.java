package com.lmlasmo.tasklist.service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.auth.PasswordRecoveryDTO;
import com.lmlasmo.tasklist.dto.create.CreateUserDTO;
import com.lmlasmo.tasklist.exception.ResourceAlreadyExistsException;
import com.lmlasmo.tasklist.exception.ResourceNotDeletableException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.mapper.UserMapper;
import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.UserRepository;
import com.lmlasmo.tasklist.repository.summary.UserSummary.StatusSummary;
import com.lmlasmo.tasklist.service.applier.UpdateUserApplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class UserService {

	@Getter private UserRepository repository;
	private UserEmailService userEmailService;
	private PasswordEncoder encoder;
	
	private UserMapper userMapper;
	
	public Mono<UserDTO> save(CreateUserDTO signup) {
		return repository.existsByUsername(signup.getUsername())
				.filter(e -> !e)
				.switchIfEmpty(Mono.error(new ResourceAlreadyExistsException("Username already exists")))
				.thenReturn(signup)
				.doOnNext(s -> s.setPassword(encoder.encode(s.getPassword())))
				.map(userMapper::toUser)
				.flatMap(repository::save)
				.map(userMapper::toDTO)
				.flatMap(u -> {
					return userEmailService.save(signup.getEmail(), u.getId())
							.thenReturn(u);
				}).as(m -> repository.getOperator().transactional(m));
	}
	
	public Mono<Void> updateUsername(int id, String username) {
		return repository.existsByUsername(username)
				.filter(e -> !e)
				.switchIfEmpty(Mono.error(new ResourceAlreadyExistsException(username + " already used")))
				.flatMap(e -> repository.findById(id))
				.doOnNext(u -> u.setUsername(username))
				.flatMap(repository::save)
				.switchIfEmpty(Mono.error(new OptimisticLockingFailureException("User was updated by another transaction")))
				.then();
	}
		
	public Mono<Void> updatePassword(PasswordRecoveryDTO passwordRecovery) {
		return repository.findByEmail(passwordRecovery.getEmail())
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
				.doOnNext(u -> UpdateUserApplier.applyPassword(passwordRecovery.getPassword(), u, encoder))
				.flatMap(repository::save)
				.switchIfEmpty(Mono.error(new OptimisticLockingFailureException("User was updated by another transaction")))
				.then();
	}
	
	public Mono<Void> updatePassword(int id, String password) {
		return repository.findById(id)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
				.doOnNext(u -> UpdateUserApplier.applyPassword(password, u, encoder))
				.flatMap(repository::save)
				.then();
	}
	
	public Mono<Void> changeStatus(int id, UserStatusType status) {
		return repository.findById(id)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
				.doOnNext(u -> u.setStatus(status))
				.flatMap(repository::save)
				.then();
	}
	
	public Mono<Void> lastLoginToNow(int id) {
		return repository.findById(id)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
				.doOnNext(u -> u.setLastLogin(Instant.now()))
				.flatMap(repository::save)
				.then();
	}
	
	public Mono<Void> markUsersInactive(Duration expires) {
		Instant after = OffsetDateTime.now().withSecond(0).withNano(0).minus(expires).toInstant();
		
		return repository.findStatusSummaryByStatusAndLastLoginAfter(UserStatusType.ACTIVE, after)
				.map(StatusSummary::getId)
				.collectList()
				.flatMap(ids -> repository.changeStatusByIds(ids, UserStatusType.INACTIVE))
				.then();
	}
	
	public Mono<Void> markInactiveUsersForDeletion() {
		return repository.findStatusSummaryByStatus(UserStatusType.INACTIVE)
				.map(StatusSummary::getId)
				.collectList()
				.flatMap(ids -> repository.changeStatusByIds(ids, UserStatusType.DELETED))
				.then();
	}
	
	public Mono<Void> deleteUsersMarkedForDeletion() {
		return repository.findStatusSummaryByStatus(UserStatusType.DELETED)
				.map(StatusSummary::getId)
				.collectList()
				.flatMap(ids -> repository.deleteAllById(ids))
				.then();
	}
	
	public Mono<Void> delete(int id) {
		return repository.existsById(id)
				.filter(Boolean::valueOf)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
				.flatMap(e -> repository.deleteById(id))
				.flatMap(v -> repository.existsById(id))
				.filter(e -> !e)
				.switchIfEmpty(Mono.error(new ResourceNotDeletableException("User not deleted")))
				.then(); 		
	}
	
	public Mono<Boolean> existsById(int id) {
		return repository.existsById(id);
	}
	
	public Mono<Boolean> existsByIdAndVersion(int id, long version) {
		return repository.existsByIdAndVersion(id, version);
	}
	
	public Mono<UserDTO> findById(int id) {
		return repository.findById(id)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
				.map(userMapper::toDTO);
	}
	
	public Mono<UserDTO> findByEmail(String email) {
		return repository.findByEmail(email)
				.switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
				.map(userMapper::toDTO);
	}	
	
}
