package com.lmlasmo.tasklist.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
@ConfigurationProperties(prefix = "app.cache.caffeine")
@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CacheCaffeineProperties {
	
	private long maximumWeight;
	private Expire expire;
	
	@Getter
    @AllArgsConstructor
    public static class Expire {
        private long afterWrite;
        private long afterAccess;
    }

}
