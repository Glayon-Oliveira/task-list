package com.lmlasmo.tasklist.advice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class ServerAdviceController {
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public Mono<Map<String, Object>> exceptionResponse(Exception ex, ServerHttpRequest req){
		ex.printStackTrace();
		return Mono.just(AdviceWrapper.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, req));
	}	
	
}
