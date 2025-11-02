package com.lmlasmo.tasklist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lmlasmo.tasklist.model.UserEmail;

public interface UserEmailRepository extends JpaRepository<UserEmail, Integer> {

	public Optional<UserEmail> findByEmail(String email);
	
	public List<UserEmail> findByUserId(int userId);
	
	public boolean existsByEmail(String email);
	
	public boolean existsByIdAndUserId(int emailId, int userId);

	public boolean existsByEmailAndUserId(String email, int userId);

	
}
