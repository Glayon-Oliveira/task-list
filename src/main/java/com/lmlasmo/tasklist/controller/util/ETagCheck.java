package com.lmlasmo.tasklist.controller.util;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.lmlasmo.tasklist.exception.PreconditionFailedException;

import reactor.core.publisher.Mono;

public interface ETagCheck {
	
	private static Set<HttpMethod> getIfMatchMathods() {
		return Set.of(PUT, PATCH, DELETE);
	}
	
	public static Mono<Boolean> check(ServerWebExchange exchange, Function<Long, Mono<Boolean>> check) {
		ServerHttpRequest req = exchange.getRequest();
		ServerHttpResponse res = exchange.getResponse();
		
		Mono<Long> etag = ETagCheck.extractEtag(req);
		
		return etag.filter(e -> e > 0)
				.flatMap(e -> {
					if(getIfMatchMathods().contains(req.getMethod())) {
						return checkPrecondition(e, check);
					}else if(req.getMethod().equals(GET)) {
						return checkIfNotModified(e, res, check);
					}
					return Mono.empty();
				}).defaultIfEmpty(true);
	}
	
	private static Mono<Boolean> checkPrecondition(long etag, Function<Long, Mono<Boolean>> check) {
		return Mono.just(etag)
				.flatMap(check::apply)
				.flatMap(c -> {
					if(!c) return Mono.error(new PreconditionFailedException(""));
					return Mono.just(c);
				});
	}

	private static Mono<Boolean> checkIfNotModified(Long etag, ServerHttpResponse res, Function<Long, Mono<Boolean>> check) {
		return Mono.just(etag)
				.flatMap(check::apply)
				.flatMap(c -> {
					if(c) res.setRawStatusCode(304);
					return Mono.just(!c);
				});
    }
	
	private static Mono<Long> extractEtag(ServerHttpRequest req) {
		List<String> match = null;
		
		try {
			if(getIfMatchMathods().contains(req.getMethod())) {
				match = req.getHeaders().getIfMatch();
			}else if(req.getMethod().equals(GET)) {
				match = req.getHeaders().getIfNoneMatch();
			}
		}catch(Exception e) {}	
		
		if(match == null || match.isEmpty()) return Mono.just(-1L);
		
		try {
			return Mono.just(Long.parseLong(match.get(0).replace("\"", "")));
		}catch(Exception e) {
			return Mono.just(-1L);
		}
	}
	
}
