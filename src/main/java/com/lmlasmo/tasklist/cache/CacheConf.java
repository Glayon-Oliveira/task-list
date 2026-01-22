package com.lmlasmo.tasklist.cache;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
@EnableConfigurationProperties({CacheCaffeineProperties.class})
public class CacheConf {

	@Bean
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
	public Cache cache(CacheManager cacheManager) {
		return cacheManager.getCache("cache");
	}
	
}
