package com.lmlasmo.tasklist.repository.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;

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
	void finds() {
		int size = getUserRepository().findStatusSummaryByStatus(UserStatusType.ACTIVE).size();
		
		assertTrue(size > 0);
		
		OffsetDateTime after = OffsetDateTime.now();
		after = after.withSecond(0).withNano(0).minusMonths(5);
		
		size = getUserRepository().findStatusSummaryByStatusAndLastLoginAfter(UserStatusType.ACTIVE, after.toInstant()).size();
		
		assertTrue(size > 0);
	}
	
	@Test
	void changeStatus() {
		List<Integer> ids = getUsers().stream()
				.map(User::getId)
				.toList();
		
		ids = new ArrayList<>(ids);
		
		getUsers().forEach(em::detach);
		
		getUserRepository().changeStatusByIds(ids, UserStatusType.INACTIVE);
		
		getUserRepository().findAll()
			.forEach(u -> assertTrue(u.getStatus().equals(UserStatusType.INACTIVE)));
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
		
		getUserRepository().deleteAllByIdInBatch(ids);
	}

}
