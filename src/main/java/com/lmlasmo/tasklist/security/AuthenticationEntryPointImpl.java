package com.lmlasmo.tasklist.security;

import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class AuthenticationEntryPointImpl implements ServerAuthenticationEntryPoint {
	
	private ObjectMapper mapper;

	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
		ServerHttpRequest req = exchange.getRequest();
		ServerHttpResponse res = exchange.getResponse();
		
		Map<String, Object> exceptionResponse = AdviceWrapper.wrapper(HttpStatus.UNAUTHORIZED, authException.getMessage(), req);		
		try {
			byte[] jsonResponse = mapper.writeValueAsBytes(exceptionResponse);
			
			res.setStatusCode(HttpStatus.UNAUTHORIZED);
			res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			
			DataBuffer buffer = res.bufferFactory().wrap(jsonResponse);			
			return res.writeWith(Mono.just(buffer));
		} catch (Exception e) {
			return res.setComplete();
		}
	}

}
