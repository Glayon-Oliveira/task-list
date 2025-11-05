package com.lmlasmo.tasklist;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.lmlasmo.tasklist.service.EmailService;

@SpringBootTest
public class TaskListApplicationTests {
	
	@MockitoBean
	private EmailService emailService;

	@Test
	public void contextLoads() {}

}
