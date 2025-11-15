package com.lmlasmo.tasklist.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.UserEmailRepository;
import com.lmlasmo.tasklist.repository.UserRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private UserRepository repository;
	private UserEmailRepository emailRepository;

	@Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		User user = null;
		
		if(login.contains("@")) {
			user = repository.findByEmail(login)
					.switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
					.block();
			
			emailRepository.existsByEmailAndStatusIn(login, EmailStatusType.getAllowedStatus())
				.filter(Boolean::booleanValue)
				.switchIfEmpty(Mono.error(new DisabledException("Email is not active")))
				.block();
		}else {
			user = repository.findByUsername(login)
					.switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
					.block();
		}
		
		return user;
	}

}
