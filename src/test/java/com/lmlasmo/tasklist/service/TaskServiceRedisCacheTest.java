package com.lmlasmo.tasklist.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.async.AsyncConf;
import com.lmlasmo.tasklist.cache.CacheConf;
import com.lmlasmo.tasklist.data.conf.AbstractRedisContainerIntegrationTest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@Import({
    TaskService.class,
    CacheConf.class,
    AsyncConf.class,
    RedisAutoConfiguration.class,
    ObjectMapper.class    
})
@ImportTestcontainers(AbstractRedisContainerIntegrationTest.class)
@ImportAutoConfiguration(TaskExecutionAutoConfiguration.class)
@ComponentScan(basePackages = "com.lmlasmo.tasklist.mapper")
@TestPropertySource(properties = {
	"app.cache.type=redis",
	"app.cache.redis.ttl=60",
})
public class TaskServiceRedisCacheTest extends AbstractTaskServiceCacheTest {}
