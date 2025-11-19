package com.lmlasmo.tasklist.repository.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.repository.UserEmailRepository;
import com.lmlasmo.tasklist.repository.UserRepository;

import lombok.Getter;

@DataR2dbcTest
public class AbstractUserRepositoryTest {

	@Getter
	@Autowired
	private UserRepository userRepository;
	
	@Getter
	@Autowired
	private UserEmailRepository userEmailRepository;

	@Getter
	private final int maxUsers = 5;

	@Getter
	private List<User> users = new ArrayList<>();
	
	@Getter
	private Map<Integer, UserEmail> emails = new HashMap<>();

	@Getter
	private Map<Integer, String> passwords = new HashMap<>();

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@BeforeEach
	public void setUp() {
		for(int cc = 0; cc < maxUsers; cc++) {
			String username = "Username - Id = " + UUID.randomUUID().toString();
			String password = "Password - Id = " + UUID.randomUUID().toString();

			User user = new User();
			user.setUsername(username);
			user.setPassword(encoder.encode(password));
			user.setLastLogin(Instant.now());
			user = userRepository.save(user).block();
			
			UserEmail userEmail = new UserEmail(UUID.randomUUID().toString()+"@example.com");
			userEmail.setUserId(user.getId());
			
			userEmail = userEmailRepository.save(userEmail).block();
			
			emails.put(user.getId(), userEmail);
			
			users.add(user);
			passwords.put(user.getId(), password);
		}

		users = Collections.unmodifiableList(users);
		passwords = Collections.unmodifiableMap(passwords);
	}
	
	@AfterEach
	public void setDown() {
		userRepository.deleteAll().block();
	}

}
