package com.lmlasmo.tasklist.advice.exception;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;

import reactor.core.publisher.Mono;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SecurityAdviceController {

	@ExceptionHandler(exception = {AuthenticationException.class})		
	@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
	public Mono<Map<String, Object>> exceptionResponse(AuthenticationException exception, ServerHttpRequest request) {
		return Mono.just(AdviceWrapper.wrapper(HttpStatus.UNAUTHORIZED, exception.getMessage(), request));		
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(code = HttpStatus.FORBIDDEN)
	public Mono<Map<String, Object>> exceptionResponse(AccessDeniedException ex, ServerHttpRequest req){
		return Mono.just(AdviceWrapper.wrapper(HttpStatus.FORBIDDEN, ex.getMessage(), req));
	}
	
}
