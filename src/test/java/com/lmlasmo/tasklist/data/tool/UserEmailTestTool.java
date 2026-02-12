package com.lmlasmo.tasklist.data.tool;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.lmlasmo.tasklist.model.EmailStatusType;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserEmail;
import com.lmlasmo.tasklist.repository.UserEmailRepository;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

@Component
@Lazy
public class UserEmailTestTool {
	
	@Autowired
	private UserEmailRepository userEmailRepository;
	
	@Autowired
	private UserTestTool userTestTool;
	
	@Getter private final String defaultEmail = "email6526@example.test";
	
	@PostConstruct
	protected void clear() {
		userEmailRepository.deleteAll().block();
	}
	
	public void runWithUser(User user, Consumer<UserEmail> consumerUserEmail) {
		UserEmail userEmail = generateUserEmailEntity(user);
		consumerUserEmail.accept(userEmail);
		
		userEmailRepository.deleteAll().block();
	}

	public void runWithUserAndEmail(User user, String email, Consumer<UserEmail> consumerUserEmail) {
		UserEmail userEmail = generateUserEmailEntity(user, email);
		consumerUserEmail.accept(userEmail);
		
		userEmailRepository.deleteAll().block();
	}
	
	public void runWithUserAndEmails(User user, Set<String> emails, Consumer<Set<UserEmail>> consumerUserEmail) {
		Set<UserEmail> userEmails = emails.stream()
				.map(e -> generateUserEmailEntity(user, e))
				.collect(Collectors.toSet());
		
		consumerUserEmail.accept(userEmails);
		
		userEmailRepository.deleteAll().block();
	}
	
	public void runWithEmail(String email, Consumer<UserEmail> consumerUserEmail) {
		userTestTool.runWithUniqueUser(u -> {
			UserEmail userEmail = generateUserEmailEntity(u, email);
			consumerUserEmail.accept(userEmail);
		});
		
		userEmailRepository.deleteAll().block();
	}
	
	public void runWithUniqueEmail(Consumer<UserEmail> consumerUserEmail) {
		userTestTool.runWithUniqueUser(u -> {
			UserEmail userEmail = generateUserEmailEntity(u);
			consumerUserEmail.accept(userEmail);
		});
		
		userEmailRepository.deleteAll().block();
	}
	
	public void runWithDuoUserEmails(BiConsumer<UserEmail, UserEmail> consumerDuoUserEmails) {
		userTestTool.runWithDuoUsers((fu, su) -> {
			UserEmail firstUserEmail = generateUserEmailEntity(fu);
			UserEmail secondUserEmail = generateUserEmailEntity(su);
			consumerDuoUserEmails.accept(firstUserEmail, secondUserEmail);
		});
		
		userEmailRepository.deleteAll().block();
	}
	
	private UserEmail generateUserEmailEntity(User user) {
		return generateUserEmailEntity(user, defaultEmail);
	}
	
	private UserEmail generateUserEmailEntity(User user, String email) {
		UserEmail userEmail = new UserEmail();
		userEmail.setEmail(email);
		userEmail.setPrimary(true);
		userEmail.setStatus(EmailStatusType.ACTIVE);
		userEmail.setUserId(user.getId());
		
		return userEmailRepository.save(userEmail).block();
	}
	
}
