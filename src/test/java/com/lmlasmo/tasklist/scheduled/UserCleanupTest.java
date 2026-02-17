package com.lmlasmo.tasklist.scheduled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;

import com.lmlasmo.tasklist.TaskListApplicationTests;
import com.lmlasmo.tasklist.data.tool.UserTestTool;

@TestInstance(Lifecycle.PER_CLASS)
public class UserCleanupTest extends TaskListApplicationTests {

	@Autowired
	private UserCleanup cleanup;
	
	@Autowired
	private UserTestTool userTestTool;
	
	@Test
	void markUsersInactive() {
		userTestTool.runWithNUsers(20, us -> {
			cleanup.taskMarkInactiveForDeletion();
		});
	}
	
	@Test
	void markInactiveForDeletion() {
		userTestTool.runWithNUsers(20, us -> {
			cleanup.taskMarkInactiveForDeletion();
		});
	}
	
	@Test
	void deleteUsersMarked() {
		userTestTool.runWithNUsers(20, us -> {
			cleanup.taskDeleteUsersMarked();
		});
	}
	
	@Test
	void deletionFlow() {
		userTestTool.runWithNUsers(20, us -> {
			cleanup.taskMarkInactiveForDeletion();
			cleanup.taskMarkInactiveForDeletion();
			cleanup.taskDeleteUsersMarked();
		});
	}
	
}
