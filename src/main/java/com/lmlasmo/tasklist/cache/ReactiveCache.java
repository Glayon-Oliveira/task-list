package com.lmlasmo.tasklist.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.core.ParameterizedTypeReference;

import reactor.core.publisher.Mono;

public interface ReactiveCache {
	
	public Mono<ValueWrapper> get(Object key);
	
	public <T> Mono<T> get(Object key, ParameterizedTypeReference<T> type);
	
	public <T> Mono<T> get(Object key, Callable<T> valueLoader);
	
	public <T> Mono<T> put(Object key, T value);
	
	public void asyncPut(Object key, Object value);
	
	public Mono<Void> evict(Object key);
	
	public <T> Mono<T> evict(Object key, ParameterizedTypeReference<T> type);
	
	public Mono<Boolean> evictIfPresent(Object key);
	
	public void asyncEvict(Object key);
	
	public Mono<Void> clear();
	
	public void asyncClear();
	
}
