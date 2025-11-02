package com.lmlasmo.tasklist.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.create.SignupDTO;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserEmail;

public class UserMapperTest {

	@Test
	void signupDTOtoUser() {
		String username = "Username - ID = " + UUID.randomUUID().toString();
		String password = "Password - ID = " + UUID.randomUUID().toString();

		SignupDTO signup = new SignupDTO();
		signup.setUsername(username);
		signup.setPassword(password);
		signup.setEmail("test@example.com");

		User user = new User(signup);

		assertTrue(user.getUsername().equals(username));
		assertTrue(user.getPassword().equals(password));
		assertTrue(user.getEmails().stream().anyMatch(e -> e.getEmail().equals("test@example.com")));
	}

	@Test
	void userToUserDTO() {
		String username = "Username - ID = " + UUID.randomUUID().toString();
		int id = 1;

		User user = new User(id);
		user.setUsername(username);
		user.setCreatedAt(Instant.now());
		user.setUpdatedAt(user.getCreatedAt());
		user.setEmails(Set.of(new UserEmail("test@example.com")));
		
		UserDTO dto = new UserDTO(user);

		assertTrue(dto.getUsername().equals(user.getUsername()));
		assertTrue(dto.getCreatedAt().equals(user.getCreatedAt()));
		assertTrue(dto.getUpdatedAt().equals(user.getUpdatedAt()));
		assertTrue(dto.getRole().equals(user.getRole()));
		assertTrue(dto.getEmails().stream().anyMatch(e -> e.getEmail().equals("test@example.com")));
	}

}
