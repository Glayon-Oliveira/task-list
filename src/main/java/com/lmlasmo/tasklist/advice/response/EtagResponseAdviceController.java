package com.lmlasmo.tasklist.advice.response;

import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;

import com.lmlasmo.tasklist.dto.VersionedDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Order(-1000)
public class EtagResponseAdviceController extends ResponseBodyResultHandler {
		
	public EtagResponseAdviceController(ServerCodecConfigurer configurer, RequestedContentTypeResolver resolver) {
		super(configurer.getWriters(), resolver);
	}
	
	@Override
	protected Mono<Void> writeBody(Object body, MethodParameter bodyParameter, ServerWebExchange exchange) {
		return Mono.justOrEmpty(body)
				.flatMap(this::extractVersion)
				.doOnNext(v -> setETag(v, exchange))
				.then(super.writeBody(body, bodyParameter, exchange));
	}
	
	private Mono<Long> extractVersion(Object body) {
		if (body instanceof VersionedDTO dto) return Mono.just(dto.getVersion());
		
		if(body instanceof List<?> list) return extractVersion(Flux.fromIterable(list));
				
	    if (body instanceof ResponseEntity<?> entity) return extractVersion(entity.getBody());
	    
	    if(body instanceof Mono<?> mono) return mono.flatMap(this::extractVersion);
	    
	    if(body instanceof Flux<?> flux) {
	    	return flux.flatMap(this::extractVersion)
	    			.reduce(Long::sum)
	    			.defaultIfEmpty(-1L);
	    }
	    
	    return Mono.just(-1L);
	}
	
	private void setETag(long version, ServerWebExchange exchange) {
		ServerHttpResponse res = exchange.getResponse();
		
		if(version >= 0) res.getHeaders().setETag("\""+version+"\"");
	}

}
