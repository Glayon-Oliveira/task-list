package com.lmlasmo.tasklist.service;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.create.SignupDTO;
import com.lmlasmo.tasklist.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.exception.EntityNotUpdateException;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.UserRepository;
import com.lmlasmo.tasklist.repository.summary.UserSummary.StatusSummary;
import com.lmlasmo.tasklist.service.applier.UpdateUserApplier;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserService {

	private UserRepository repository;
	private PasswordEncoder encoder;
	
	public UserDTO save(SignupDTO signup) {
		if(repository.existsByUsername(signup.getUsername())) throw new EntityExistsException("User already used");
		
		signup.setPassword(encoder.encode(signup.getPassword()));
		User user = new User(signup);		
		user = repository.save(user);
		return new UserDTO(user);
	}
	
	public void updateUsername(int id, String username) {
		User user = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		if(repository.existsByUsername(username)) throw new EntityNotUpdateException(username + " already used");
		
		user.setUsername(username);		
		repository.save(user);
	}
	
	public void updatePassword(int id, String password) {		
		User user = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		UpdateUserApplier.applyPassword(password, user, encoder);		
		repository.save(user);
	}
	
	public void changeStatus(int id, UserStatusType status) {
		User user = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		user.setStatus(status);		
		repository.save(user);
	}
	
	public void lastLoginToNow(int id) {
		User user = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		user.setLastLogin(Instant.now());		
		repository.save(user);
	}
	
	public void markUsersInactive(Duration expires) {
		OffsetDateTime after = OffsetDateTime.now();
		after = after.withSecond(0).withNano(0).minus(expires);
		
		List<Integer> ids = repository.findStatusSummaryByStatusAndLastLoginAfter(UserStatusType.ACTIVE, after.toInstant())
				.stream()
				.map(StatusSummary::getId)
				.toList();
		
		repository.changeStatusByIds(ids, UserStatusType.INACTIVE);
	}
	
	public void markInactiveUsersForDeletion() {
		List<Integer> ids = repository.findStatusSummaryByStatus(UserStatusType.INACTIVE)
				.stream()
				.map(StatusSummary::getId)
				.toList();
		
		repository.changeStatusByIds(ids, UserStatusType.DELETED);
	}
	
	public void deleteUsersMarkedForDeletion() {
		List<Integer> ids = repository.findStatusSummaryByStatus(UserStatusType.DELETED)
				.stream()
				.map(StatusSummary::getId)
				.toList();
		
		repository.deleteAllByIdInBatch(ids);
	}
	
	public void delete(int id) {
		if(!repository.existsById(id)) throw new EntityNotFoundException("User not found");
		
		repository.deleteById(id);
		
		if(repository.existsById(id)) throw new EntityNotDeleteException("User not deleted"); 		
	}
	
	public boolean existsById(int id) {
		return repository.existsById(id);
	}
	
	public boolean existsByIdAndVersion(int id, long version) {
		return repository.existsByIdAndVersion(id, version);
	}
	
	public UserDTO findById(int id) {		
		UserDTO user = repository.findById(id)
				.map(UserDTO::new)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		return user;
	}
	
	public UserDTO findByEmail(String email) {		
		UserDTO user = repository.findByEmailsEmail(email)
				.map(UserDTO::new)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		return user;
	}	
	
}
