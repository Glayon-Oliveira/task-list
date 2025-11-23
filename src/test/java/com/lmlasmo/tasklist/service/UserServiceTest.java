package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
import org.springframework.transaction.reactive.TransactionalOperator;

import com.lmlasmo.tasklist.dto.UserEmailDTO;
import com.lmlasmo.tasklist.dto.create.CreateUserDTO;
import com.lmlasmo.tasklist.exception.ResourceNotDeletableException;
import com.lmlasmo.tasklist.exception.ResourceNotUpdatableException;
import com.lmlasmo.tasklist.exception.ResourceAlreadyExistsException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.repository.UserRepository;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	
	@Mock
	private UserEmailService userEmailService;
	
	@Mock
	private TransactionalOperator operator;

	private PasswordEncoder encoder = new BCryptPasswordEncoder();

	private UserService userService;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void setUpAll() {
		userService = new UserService(userRepository, userEmailService, encoder);
		lenient().when(userRepository.getOperator()).thenReturn(operator);
		lenient().when(operator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void create() {
		String username = "Username - Id = " + UUID.randomUUID().toString();
		String password = "Password - Id = " + UUID.randomUUID().toString();

		CreateUserDTO signup = new CreateUserDTO();
		signup.setUsername(username);
		signup.setPassword(password);
		signup.setEmail("test@example.com");

		User user = new User(signup);
		user.setId(1);
		user.setPassword(encoder.encode(password));

		when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
		when(userRepository.existsByUsername(username)).thenReturn(Mono.just(true));
		when(userEmailService.save(anyString(), anyInt())).thenReturn(Mono.just(new UserEmailDTO(new UserEmail())));

		assertThrows(ResourceAlreadyExistsException.class, () -> userService.save(signup).block());

		when(userRepository.existsByUsername(username)).thenReturn(Mono.just(false));

		assertDoesNotThrow(() -> userService.save(signup).block());
	}

	@Test
	void updatePassword() {
		String password = "Password - Id = " + UUID.randomUUID().toString();
		int id = 1;
		int nId = 2;

		User user = new User(id);
		user.setPassword(encoder.encode(password));

		when(userRepository.findById(id)).thenReturn(Mono.just(user));
		when(userRepository.findById(nId)).thenReturn(Mono.empty());
		when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

		assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword(nId, password).block());
		assertThrows(ResourceNotUpdatableException.class, () -> userService.updatePassword(id, password).block());

		user.setPassword(encoder.encode(UUID.randomUUID().toString()));

		assertDoesNotThrow(() -> userService.updatePassword(id, password).block());
	}

	@Test
	void deleteUser() {
		int id = 1;
		int nId = 2;

		when(userRepository.existsById(id)).thenReturn(Mono.just(true));
		when(userRepository.existsById(nId)).thenReturn(Mono.just(false));
		when(userRepository.deleteById(anyInt())).thenReturn(Mono.empty());

		assertThrows(ResourceNotFoundException.class, () -> userService.delete(nId).block());
		assertThrows(ResourceNotDeletableException.class, () -> userService.delete(id).block());
	}

	@Test
	void findUser() {
		int id = 1;
		int nId = 2;

		User user = new User(id);

		when(userRepository.findById(id)).thenReturn(Mono.justOrEmpty(user));
		when(userRepository.findById(nId)).thenReturn(Mono.empty());

		assertThrows(ResourceNotFoundException.class, () -> userService.findById(nId).block());
		assertDoesNotThrow(() -> userService.findById(id).block());
	}

}
