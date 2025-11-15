package com.lmlasmo.tasklist.repository.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.model.UserStatusType;

public class UserRepositoryTest extends AbstractUserRepositoryTest {

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Test
	void updatePassword() {
		String newPassword = encoder.encode(UUID.randomUUID().toString());

		getUsers().forEach(u -> {
			u.setPassword(newPassword);
			getUserRepository().save(u).block();

			String password = getUserRepository().findById(u.getId()).block().getPassword();

			assertTrue(password.equals(newPassword));
		});
	}
	
	@Test
	void findByStatus() {
		int size = getUserRepository().findStatusSummaryByStatus(UserStatusType.ACTIVE)
				.collectList().block()
				.size();
		
		assertTrue(size > 0);
		
		OffsetDateTime after = OffsetDateTime.now();
		after = after.withSecond(0).withNano(0).minusMonths(5);
		
		size = getUserRepository().findStatusSummaryByStatusAndLastLoginAfter(UserStatusType.ACTIVE, after.toInstant())
				.collectList().block()
				.size();
		
		assertTrue(size > 0);
	}
	
	@Test
	void changeStatus() {
		List<Integer> ids = getUsers().stream()
				.map(User::getId)
				.toList();
		
		ids = new ArrayList<>(ids);
		
		getUserRepository().changeStatusByIds(ids, UserStatusType.INACTIVE);
		
		getUserRepository().findAll()
			.doOnNext(u -> assertTrue(u.getStatus().equals(UserStatusType.INACTIVE)));
	}

	@Test
	void delete() {
		getUsers().forEach(u -> {
			getUserRepository().deleteById(u.getId());
		});
	}
	
	@Test
	void deleteBatch() {
		List<Integer> ids = getUsers().stream()
				.map(User::getId)
				.toList();
		
		getUserRepository().deleteAllById(ids);
	}
	
	@Test
	void findBy() {
		getUsers().forEach(u -> {
			User user = getUserRepository().findByUsername(u.getUsername()).block();
			
			assertEquals(user.getUsername(), u.getUsername());
			
			UserEmail email = getEmails().get(u.getId());			
			user = getUserRepository().findByEmail(email.getEmail()).block();
			
			assertEquals(user.getId(), email.getUserId());
		});
	}

}
