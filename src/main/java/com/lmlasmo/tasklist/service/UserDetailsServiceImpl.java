package com.lmlasmo.tasklist.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private UserRepository repository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = repository.findByUsername(username).orElseGet(null);
				
		if(user == null) throw new UsernameNotFoundException("User not found"); 
		
		return user;
	}

}
