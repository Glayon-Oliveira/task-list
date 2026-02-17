package com.lmlasmo.tasklist.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@AllArgsConstructor
public class ReactiveCaffeineCache implements ReactiveCache {

	private CaffeineCache cache;
	
	@Override
	public Mono<ValueWrapper> get(Object key) {
		return Mono.justOrEmpty(cache.get(key));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Mono<T> get(Object key, ParameterizedTypeReference<T> type) {
		return Mono.justOrEmpty(cache.get(key))
				.map(v -> (T) v.get());
	}
	
	@Override
	public <T> Mono<T> get(Object key, Callable<T> valueLoader) {
		return Mono.justOrEmpty(cache.get(key, valueLoader));
	}
	
	@Override
	public <T> Mono<T> put(Object key, T value) {
		return Mono.fromRunnable(() -> cache.put(key, value))
				.thenReturn(value);
	}
	
	@Override
	@Async
	public void asyncPut(Object key, Object value) {
		cache.put(key, value);
	}

	@Override
	public <T> Mono<T> evict(Object key, ParameterizedTypeReference<T> type) {
		return Mono.zip(get(key, type), evict(key))
				.map(Tuple2::getT1);
	}

	@Override
	public Mono<Boolean> evictIfPresent(Object key) {
		return Mono.just(cache.evictIfPresent(key));
	}
	
	@Override
	public Mono<Void> evict(Object key) {
		return Mono.fromRunnable(() -> cache.evict(key));
	}

	@Override
	@Async
	public void asyncEvict(Object key) {
		cache.evict(key);
	}
	
	@Override
	public Mono<Void> clear() {
		return Mono.fromRunnable(() -> cache.clear());
	}

	@Override
	@Async
	public void asyncClear() {
		cache.clear();
	}
	
}
