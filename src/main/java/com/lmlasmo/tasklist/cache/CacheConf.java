package com.lmlasmo.tasklist.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
@EnableConfigurationProperties({CacheCaffeineProperties.class})
public class CacheConf {

	@Bean
	@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine", matchIfMissing = true)
	public CacheManager caffeineCacheManager(CacheCaffeineProperties caffeineProperties) {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("cache");
		
		cacheManager.setCaffeine(Caffeine.newBuilder()
				.expireAfterAccess(caffeineProperties.getExpire().getAfterAccess(), TimeUnit.MINUTES)
				.expireAfterWrite(caffeineProperties.getExpire().getAfterWrite(), TimeUnit.MINUTES)
				.maximumWeight(caffeineProperties.getMaximumWeight())
				.weigher((k, v) -> {
					if (v instanceof Collection<?> collection) return collection.size();
					return 1;
				}));
		
		return cacheManager;
	}
	
	@Bean
	@ConditionalOnProperty(name = "app.cache.type", havingValue = "redis")
	public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objMapper,
			@Value("${app.cache.redis.ttl}") long ttl) {
		
		GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objMapper);
		
		return RedisCacheManager.builder(connectionFactory)
				.withCacheConfiguration(
						"cache",
						RedisCacheConfiguration.defaultCacheConfig()
							.disableCachingNullValues()
							.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
							.computePrefixWith(CacheKeyPrefix.prefixed("tasklist::"))
							.entryTtl(Duration.ofSeconds(ttl))
						)
				.build();
	}
	
	@Bean
	@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine", matchIfMissing = true)
	public ReactiveCache caffeineCache(CacheManager cacheManager) {
		return new ReactiveCaffeineCache((CaffeineCache) cacheManager.getCache("cache"));
	}
	
	@Bean
	@ConditionalOnProperty(name = "app.cache.type", havingValue = "redis")
	public ReactiveCache redisCache(CacheManager cacheManager, ObjectMapper mapper) {
		return new ReactiveRedisCache((RedisCache) cacheManager.getCache("cache"), mapper);
	}
	
}
