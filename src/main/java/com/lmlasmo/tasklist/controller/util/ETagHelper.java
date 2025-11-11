package com.lmlasmo.tasklist.controller.util;

import java.util.function.Function;

import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.server.ServerWebExchange;

import com.lmlasmo.tasklist.dto.VersionedDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ETagHelper {	
	
	public static Mono<Boolean> checkEtag(Function<Long, Mono<Boolean>> check){
		return Mono.deferContextual(ctx -> {
			ServerWebExchange exchange = ctx.get(ServerWebExchangeContextFilter.EXCHANGE_CONTEXT_ATTRIBUTE);
			
			return ETagCheck.check(exchange, check);
		});
	}
	
	public static <T extends VersionedDTO> Mono<T> setEtag(Mono<T> body) {
		return Mono.deferContextual(ctx -> {
			ServerWebExchange exchange = ctx.get(ServerWebExchangeContextFilter.EXCHANGE_CONTEXT_ATTRIBUTE);
			
			return ETagWriter.setEtag(exchange, body);
		});
	}
	
	public static <T extends VersionedDTO> Flux<T> setEtag(Flux<T> body) {
		return Flux.deferContextual(ctx -> {
			ServerWebExchange exchange = ctx.get(ServerWebExchangeContextFilter.EXCHANGE_CONTEXT_ATTRIBUTE);
			
			return ETagWriter.setEtag(exchange, body);
		});
	}
	
}
