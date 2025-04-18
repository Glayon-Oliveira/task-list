package com.lmlasml.tasklist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lmlasml.tasklist.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	public Optional<User> findByUsername(String username);
	
	public boolean existsByUsername(String username);
	
}
