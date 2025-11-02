package com.lmlasmo.tasklist.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private UserRepository repository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = null;
		
		if(username.contains("@")) {
			user = repository.findByEmailsEmail(username)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));
			
			user.getEmails().stream()
				.filter(e -> e.getEmail().equals(username) && EmailStatusType.getAllowedStatus().contains(e.getStatus()))
				.findFirst()
				.orElseThrow(() -> new DisabledException("Email is not active"));
		}else {
			user = repository.findByUsername(username)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		}
		
		return user;
	}

}
