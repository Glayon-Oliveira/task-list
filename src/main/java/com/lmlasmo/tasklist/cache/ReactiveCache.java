package com.lmlasmo.tasklist.cache;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Getter
@AllArgsConstructor
public class ReactiveCache {
	
	private Cache cache;
	private boolean elastic = false;
	
	public Mono<ValueWrapper> get(Object key) {
		return elastic
				? Mono.fromCallable(() -> cache.get(key))
						.subscribeOn(Schedulers.boundedElastic())
				: Mono.just(cache.get(key));
	}
	
	public <T> Mono<T> get(Object key, Class<T> type) {
		return elastic
				? Mono.fromCallable(() -> cache.get(key, type))
						.subscribeOn(Schedulers.boundedElastic())
				: Mono.just(cache.get(key, type));
	}
	
	public <T> Mono<T> get(Object key, Callable<T> valueLoader) {
		return elastic
				? Mono.fromCallable(() -> cache.get(key, valueLoader))
						.subscribeOn(Schedulers.boundedElastic())
				: Mono.just(cache.get(key, valueLoader));
	}
	
	public void put(Object key, Object value) {
		run(() -> cache.put(key, value));
	}
	
	public void evict(Object key) {
		run(() -> cache.evict(key));
	}
	
	public void clear() {
		run(() -> cache.clear());
	}
	
	private void run(Runnable run) {
		Schedulers.boundedElastic().schedule(run);
	}
	
}
