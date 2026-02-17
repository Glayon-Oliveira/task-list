package com.lmlasmo.tasklist.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
public class ReactiveRedisCache implements ReactiveCache {

	private RedisCache cache;
	private ObjectMapper mapper;
	
	public Mono<ValueWrapper> get(Object key) {
		return Mono.fromCallable(() -> cache.get(key))
				.subscribeOn(Schedulers.boundedElastic());
	}
	
	public <T> Mono<T> get(Object key, ParameterizedTypeReference<T> type) {
		JavaType jType = mapper.constructType(type.getType());
		
		return Mono.fromCallable(() -> cache.get(key))
				.subscribeOn(Schedulers.boundedElastic())
				.map(ValueWrapper::get)
				.map(c -> mapper.convertValue(c, jType));
	}
	
	@SuppressWarnings("unchecked")
	public <T> Mono<T> get(Object key, Callable<T> valueLoader) {
		try {
			T value = valueLoader.call();
			
			return Mono.fromCallable(() -> cache.get(key))
					.subscribeOn(Schedulers.boundedElastic())
					.map(c -> (T) mapper.convertValue(c, value.getClass()))
					.switchIfEmpty(
							Mono.fromRunnable(() -> cache.put(key, value))
							.subscribeOn(Schedulers.boundedElastic())
							.thenReturn(value)
					);
		} catch (Exception e) {
			return Mono.error(() -> e);
		}
	}
	
	@Override
	public <T> Mono<T> put(Object key, T value) {
		return Mono.fromRunnable(() -> cache.put(key, value))
				.subscribeOn(Schedulers.boundedElastic())
				.thenReturn(value);
	}
	
	@Override
	@Async
	public void asyncPut(Object key, Object value) {
		cache.put(key, value);
	}
	
	@Override
	public Mono<Void> evict(Object key) {
		return Mono.fromRunnable(() -> cache.evict(key))
				.subscribeOn(Schedulers.boundedElastic())
				.then();
	}
	
	@Override
	public <T> Mono<T> evict(Object key, ParameterizedTypeReference<T> type) {
		return get(key, type)
				.flatMap(c -> evict(key)
						.thenReturn(c)
				);
	}
	
	@Override
	public Mono<Boolean> evictIfPresent(Object key) {
		return Mono.fromCallable(() -> cache.evictIfPresent(key))
				.subscribeOn(Schedulers.boundedElastic());
	}
	
	@Override
	@Async
	public void asyncEvict(Object key) {
		cache.evict(key);
	}
	
	@Override
	public Mono<Void> clear() {
		return Mono.fromRunnable(() -> cache.clear())
				.subscribeOn(Schedulers.boundedElastic())
				.then();
	}
	
	@Override
	@Async
	public void asyncClear() {
		cache.clear();
	}
	
}
