package com.lmlasmo.tasklist.service.applier;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.lmlasmo.tasklist.exception.EntityNotUpdateException;
import com.lmlasmo.tasklist.model.User;

public interface UpdateUserApplier {

	public static void applyPassword(String password, User user, PasswordEncoder encoder) {		
		if(encoder.matches(password, user.getPassword())) throw new EntityNotUpdateException("Password already used");
		
		user.setPassword(encoder.encode(password));
	}
	
}
