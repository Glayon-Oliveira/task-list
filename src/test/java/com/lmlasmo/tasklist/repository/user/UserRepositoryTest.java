package com.lmlasmo.tasklist.repository.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.EntityManager;

public class UserRepositoryTest extends AbstractUserRepositoryTest {

	@Autowired
	private EntityManager em;

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Test
	void updatePassword() {
		String newPassword = encoder.encode(UUID.randomUUID().toString());

		getUsers().forEach(u -> {
			em.detach(u);

			u.setPassword(newPassword);
			getUserRepository().save(u);

			String password = getUserRepository().findById(u.getId())
					.orElseThrow().getPassword();

			assertTrue(password.equals(newPassword));
		});
	}

	@Test
	void delete() {
		getUsers().forEach(u -> {
			getUserRepository().deleteById(u.getId());
		});
	}

}
