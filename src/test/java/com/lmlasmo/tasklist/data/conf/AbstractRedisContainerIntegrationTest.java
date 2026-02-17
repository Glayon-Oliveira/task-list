package com.lmlasmo.tasklist.data.conf;

import java.time.Duration;

import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractRedisContainerIntegrationTest {
	
	@SuppressWarnings("resource")
	@Container
	@ServiceConnection(type = RedisConnectionDetails.class)
	public static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
			.withExposedPorts(6379)
			.withStartupTimeout(Duration.ofMinutes(10))
			.waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(10)))
			.withReuse(true);
	
	@DynamicPropertySource
	static void d(DynamicPropertyRegistry re) {
		re.add("spring.data.redis.host", AbstractRedisContainerIntegrationTest.redis::getHost);
		re.add("spring.data.redis.port", () -> AbstractRedisContainerIntegrationTest.redis.getMappedPort(6379));
		re.add("spring.data.redis.timeout", () -> "1m");
		re.add("spring.data.redis.connect-timeout", () -> "1m");
	}
	
}
