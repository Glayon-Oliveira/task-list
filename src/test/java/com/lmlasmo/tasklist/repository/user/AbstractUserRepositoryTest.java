package com.lmlasmo.tasklist.repository.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.repository.UserRepository;

import lombok.Getter;

@DataJpaTest(showSql = false)
@Transactional
public class AbstractUserRepositoryTest {

	@Getter
	@Autowired
	private UserRepository userRepository;

	@Getter
	private final int maxUsers = 5;

	@Getter
	private List<User> users = new ArrayList<>();

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
			user.setEmails(new HashSet<>());
			user.setLastLogin(Instant.now());
			
			UserEmail userEmail = new UserEmail("test@example.com");
			userEmail.setUser(user);
			
			user.getEmails().add(userEmail);

			user = userRepository.save(user);
			users.add(user);
			passwords.put(user.getId(), password);
		}

		users = Collections.unmodifiableList(users);
		passwords = Collections.unmodifiableMap(passwords);
	}

}
