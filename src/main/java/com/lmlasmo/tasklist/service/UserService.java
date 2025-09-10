package com.lmlasmo.tasklist.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.create.SignupDTO;
import com.lmlasmo.tasklist.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.exception.EntityNotUpdateException;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.UserRepository;

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
		
		if(encoder.matches(password, user.getPassword())) throw new EntityNotUpdateException("Password already used");
		
		user.setPassword(encoder.encode(password));		
		
		repository.save(user);
	}
	
	public void delete(int id) {
		if(!repository.existsById(id)) throw new EntityNotFoundException("User not found");
		
		repository.deleteById(id);
		
		if(repository.existsById(id)) throw new EntityNotDeleteException("User not deleted"); 		
	}
	
	public boolean existsById(int id) {
		return repository.existsById(id);
	}
	
	public UserDTO findById(int id) {		
		UserDTO user = repository.findById(id)
				.map(UserDTO::new)
				.orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		return user;
	}
	
}
