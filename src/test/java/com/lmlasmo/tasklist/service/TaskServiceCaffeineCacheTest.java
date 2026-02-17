package com.lmlasmo.tasklist.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lmlasmo.tasklist.cache.CacheCaffeineProperties;
import com.lmlasmo.tasklist.cache.CacheConf;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@Import({
    TaskService.class,
    CacheConf.class,
})
@ImportAutoConfiguration(TaskExecutionAutoConfiguration.class)
@ComponentScan(basePackages = "com.lmlasmo.tasklist.mapper")
@EnableConfigurationProperties(CacheCaffeineProperties.class)
public class TaskServiceCaffeineCacheTest extends AbstractTaskServiceCacheTest {}
