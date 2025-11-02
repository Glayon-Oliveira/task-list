package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lmlasmo.tasklist.dto.create.SignupDTO;
import com.lmlasmo.tasklist.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.exception.EntityNotUpdateException;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.UserRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	private PasswordEncoder encoder = new BCryptPasswordEncoder();

	private UserService userService;

	@BeforeEach
	public void setUpAll() {
		userService = new UserService(userRepository, encoder);
	}

	@Test
	void create() {
		String username = "Username - Id = " + UUID.randomUUID().toString();
		String password = "Password - Id = " + UUID.randomUUID().toString();

		SignupDTO signup = new SignupDTO();
		signup.setUsername(username);
		signup.setPassword(password);

		User user = new User(signup);
		user.setId(1);
		user.setPassword(encoder.encode(password));

		when(userRepository.save(any(User.class))).thenReturn(user);
		when(userRepository.existsByUsername(username)).thenReturn(true);

		assertThrows(EntityExistsException.class, () -> userService.save(signup));

		when(userRepository.existsByUsername(username)).thenReturn(false);

		assertDoesNotThrow(() -> userService.save(signup));
	}

	@Test
	void updatePassword() {
		String password = "Password - Id = " + UUID.randomUUID().toString();
		int id = 1;
		int nId = 2;

		User user = new User(id);
		user.setPassword(encoder.encode(password));

		when(userRepository.findById(id)).thenReturn(Optional.of(user));
		when(userRepository.findById(nId)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenReturn(user);

		assertThrows(EntityNotFoundException.class, () -> userService.updatePassword(nId, password));
		assertThrows(EntityNotUpdateException.class, () -> userService.updatePassword(id, password));

		user.setPassword(encoder.encode(UUID.randomUUID().toString()));

		assertDoesNotThrow(() -> userService.updatePassword(id, password));
	}

	@Test
	void deleteUser() {
		int id = 1;
		int nId = 2;

		when(userRepository.existsById(id)).thenReturn(true);
		when(userRepository.existsById(nId)).thenReturn(false);

		assertThrows(EntityNotFoundException.class, () -> userService.delete(nId));
		assertThrows(EntityNotDeleteException.class, () -> userService.delete(id));
	}

	@Test
	void findUser() {
		int id = 1;
		int nId = 2;

		User user = new User(id);
		user.setEmails(Set.of());

		when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));
		when(userRepository.findById(nId)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> userService.findById(nId));
		assertDoesNotThrow(() -> userService.findById(id));
	}

}
