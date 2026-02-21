package com.lmlasmo.tasklist.web.util;

import java.util.Map;
import java.util.function.LongFunction;

import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;

import com.lmlasmo.tasklist.dto.VersionedDTO;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class ETagHelper {
	
	@NonNull
	private ServerWebExchange exchange;
	
	public Mono<Boolean> checkEtag(LongFunction<Mono<Boolean>> check){
		return ETagCheck.check(exchange, check);
	}
	
	public Flux<Map<String, Object>> setETagWithMap(Flux<Map<String, Object>> body) {
		return ETagWriter.setEtagWithMap(exchange, body);
	}
	
	public Mono<Map<String, Object>> setETagWithMap(Mono<Map<String, Object>> body) {
		return ETagWriter.setEtagWithMap(exchange, body);
	}
	
	public <T extends VersionedDTO> Mono<T> setEtag(Mono<T> body) {
		return ETagWriter.setEtag(exchange, body);
	}
	
	public <T extends VersionedDTO> Flux<T> setEtag(Flux<T> body) {
		return ETagWriter.setEtag(exchange, body);
	}
	
}
