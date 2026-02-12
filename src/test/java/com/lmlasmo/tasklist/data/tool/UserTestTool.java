package com.lmlasmo.tasklist.data.tool;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.model.RoleType;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.UserRepository;

import jakarta.annotation.PreDestroy;

@Component
@Lazy
public class UserTestTool {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder encoder;
	
	private String defaultUsername = "972thjd9";
	
	@PreDestroy
	protected void destroy() {
		userRepository.deleteAll().block();
	}
	
	public void runWithCredentials(String username, String password, Consumer<User> consumerUser) {
		User user = generateUserEntity(username, password);
		
		consumerUser.accept(user);
		userRepository.deleteAll().block();
	}
	
	public void runWithNUsers(int quantity, Consumer<Set<User>> consumerUsers) {
		Set<User> users = new LinkedHashSet<User>();
		
		for(int cc = 0; cc < quantity; cc++) {
			String username = "Username="+UUID.randomUUID();
			String password = UUID.randomUUID().toString();
			
			users.add(generateUserEntity(username, password));
		}
		
		consumerUsers.accept(users);
		userRepository.deleteAll().block();
	}
	
	public void runWithUniqueUser(Consumer<User> consumerUser) {
		userRepository.deleteAll().block();
		
		User user = generateUserEntity();
		consumerUser.accept(user);
		
		userRepository.deleteAll().block();
	}
	
	public void runWithDuoUsers(BiConsumer<User, User> consumerDuoUsers) {
		User firstUser = generateUserEntity(UUID.randomUUID().toString());
		User secondUser = generateUserEntity(UUID.randomUUID().toString());
		consumerDuoUsers.accept(firstUser, secondUser);
		
		userRepository.deleteAll().block();
	}
	
	public User generateUserEntity() {
		return generateUserEntity(UUID.randomUUID().toString());
	}
	
	public User generateUserEntity(String password) {
		return generateUserEntity(defaultUsername, password);
	}
	
	public User generateUserEntity(String username, String password) {
		User user = new User();
		user.setUsername(username);
		user.setRole(RoleType.ADMIN);
		user.setPassword(encoder.encode(password));
	
		return userRepository.save(user).block();
	}
	
}
