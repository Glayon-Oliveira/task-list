package com.lmlasmo.tasklist.scheduled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;

import com.lmlasmo.tasklist.TaskListApplicationTests;

@TestInstance(Lifecycle.PER_CLASS)
public class EmailConfirmationUpdateTest extends TaskListApplicationTests {
	
	@Autowired
	private EmailConfirmationUpdate confirmationUpdate;
	
	@Test
	void update() {
		confirmationUpdate.update();
	}
	
}
