package com.lmlasmo.tasklist.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.repository.UserEmailRepository;
import com.lmlasmo.tasklist.repository.UserRepository;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {
	
	private UserRepository repository;
	private UserEmailRepository emailRepository;

	@Override
	public Mono<UserDetails> findByUsername(String login) throws UsernameNotFoundException {
		if(login.contains("@")) {
			return emailRepository.existsByEmailAndStatusIn(login, EmailStatusType.getAllowedStatus())
					.filter(Boolean::booleanValue)
					.switchIfEmpty(Mono.error(new DisabledException("Email is not active")))
					.then(repository.findByEmail(login))
					.switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
					.map(u -> (UserDetails) u);
		}else {
			return repository.findByUsername(login)
					.switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
					.map(u -> (UserDetails) u);
		}
	}

}
