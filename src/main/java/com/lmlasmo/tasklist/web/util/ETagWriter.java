package com.lmlasmo.tasklist.web.util;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.lmlasmo.tasklist.dto.VersionedDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ETagWriter {
	
	public static <T extends VersionedDTO> Mono<T> setEtag(ServerWebExchange exchange, Mono<T> body) {
		return body.flatMap(b -> {
				return ETagWriter.extractVersion(b)
						.doOnNext(v -> setETag(v, exchange))
						.map(v -> b);
		});
	}
	
	public static <T extends VersionedDTO> Flux<T> setEtag(ServerWebExchange exchange, Flux<T> body) {
		return body.collectList()
				.flatMapMany(b -> {
					return ETagWriter.extractVersion(b)
							.doOnNext(v -> setETag(v, exchange))
							.flatMapMany(v -> Flux.fromIterable(b));
				});
	}

	private static Mono<Long> extractVersion(Object body) {
		if (body instanceof VersionedDTO dto) return Mono.just(dto.getVersion());
				
	    if (body instanceof ResponseEntity<?> entity) return extractVersion(entity.getBody());
	    
	    if(body instanceof List<?> list) {
	    	return Flux.fromIterable(list)
	    			.filter(b -> b instanceof VersionedDTO)
	    			.flatMap(ETagWriter::extractVersion)
	    			.reduce(Long::sum);
	    }
	    
	    return Mono.just(-1L);
	}
	
	private static void setETag(long version, ServerWebExchange exchange) {
		ServerHttpResponse res = exchange.getResponse();
		
		if(version >= 0) res.getHeaders().setETag("\""+version+"\"");
	}
	
}
