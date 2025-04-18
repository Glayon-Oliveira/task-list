package com.lmlasmo.tasklist.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.advice.exception.EntityNotUpdateException;
import com.lmlasmo.tasklist.dto.SignupDTO;
import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.UserRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Service
@Transactional
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
	
	public UserDTO updateUsername(int id, String username) {
		User user = repository.findById(id).orElseGet(null);
		
		if(user == null) throw new EntityNotFoundException("User not found");
		
		if(repository.existsByUsername(username)) throw new EntityNotUpdateException(username + " already used");
		
		user.setUsername(username);
		return new UserDTO(repository.save(user));
	}
	
	public UserDTO updatePassword(int id, String password) {		
		User user = repository.findById(id).orElseGet(null);
		
		if(user == null) throw new EntityNotFoundException("User not found");
		
		if(encoder.matches(password, user.getPassword())) throw new EntityNotUpdateException("Password already used");
		
		user.setPassword(encoder.encode(password));		
		return new UserDTO(repository.save(user));
	}
	
	public void delete(int id) {		
		if(!repository.existsById(id)) throw new EntityNotFoundException("User not found");
		
		repository.deleteById(id);
		
		if(repository.existsById(id)) throw new EntityExistsException("User not deleted"); 		
	}
	
	public UserDTO findById(int id) {		
		UserDTO user = repository.findById(id).map(UserDTO::new).orElseGet(null);
		
		if(user == null) throw new EntityNotFoundException("User not found");
		
		return user;
	}
	
	public Page<UserDTO> findAll(Pageable pageable) {
		return repository.findAll(pageable).map(UserDTO::new);
	}	
	
}
