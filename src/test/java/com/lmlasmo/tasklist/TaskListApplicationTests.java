package com.lmlasmo.tasklist;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.lmlasmo.tasklist.data.conf.AbstractDataContainerIntegrationTest;
import com.lmlasmo.tasklist.service.EmailService;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ImportTestcontainers(AbstractDataContainerIntegrationTest.class)
public class TaskListApplicationTests {
	
	@MockitoBean
	private EmailService emailService;
	
	@BeforeEach
	protected void beforeOfWhen() {
		when(emailService.send(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
	}

	@Test
	public void contextLoads() {}

}
