package com.lmlasmo.tasklist.cache;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine", matchIfMissing = true)
@EnableAutoConfiguration(exclude = {
		RedisAutoConfiguration.class, 
		RedisReactiveAutoConfiguration.class, 
		})
public class NoRedisAutoConf {}
