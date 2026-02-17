package com.lmlasmo.tasklist.data.conf;

import java.time.Duration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractDataContainerIntegrationTest {
	
	@SuppressWarnings("resource")
	@ServiceConnection
	@Container
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
			.withDatabaseName("tasklisttestdb")
			.withUsername("testpass")
			.withPassword("testpass")
			.withConnectTimeoutSeconds((int) Duration.ofMinutes(10).toSeconds())
			.withStartupTimeout(Duration.ofMinutes(10))
			.withReuse(true);
	
}
