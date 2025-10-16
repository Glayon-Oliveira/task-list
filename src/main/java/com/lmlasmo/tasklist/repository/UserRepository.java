package com.lmlasmo.tasklist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lmlasmo.tasklist.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	public Optional<User> findByUsername(String username);
	
	public boolean existsByUsername(String username);

	public boolean existsByIdAndVersion(int id, long version);
	
}
