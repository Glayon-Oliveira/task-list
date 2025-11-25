package com.lmlasmo.tasklist.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lmlasmo.tasklist.dto.UserDTO;
import com.lmlasmo.tasklist.dto.create.CreateUserDTO;
import com.lmlasmo.tasklist.model.User;

@ExtendWith(SpringExtension.class)
@Import(MapperTestConfig.class)
public class UserMapperTest {
	
	@Autowired
	private UserMapper mapper;

	@Test
	void signupDTOtoUser() {
		String username = "Username - ID = " + UUID.randomUUID().toString();
		String password = "Password - ID = " + UUID.randomUUID().toString();

		CreateUserDTO signup = new CreateUserDTO();
		signup.setUsername(username);
		signup.setPassword(password);
		signup.setEmail("test@example.com");
		
		User user = mapper.toUser(signup);

		assertTrue(user.getUsername().equals(username));
		assertTrue(user.getPassword().equals(password));
	}

	@Test
	void userToUserDTO() {
		String username = "Username - ID = " + UUID.randomUUID().toString();
		int id = 1;

		User user = new User(id);
		user.setUsername(username);
		user.setCreatedAt(Instant.now());
		user.setUpdatedAt(user.getCreatedAt());
		
		UserDTO dto = mapper.toDTO(user);

		assertTrue(dto.getUsername().equals(user.getUsername()));
		assertTrue(dto.getCreatedAt().equals(user.getCreatedAt()));
		assertTrue(dto.getUpdatedAt().equals(user.getUpdatedAt()));
		assertTrue(dto.getRole().equals(user.getRole()));
	}

}
