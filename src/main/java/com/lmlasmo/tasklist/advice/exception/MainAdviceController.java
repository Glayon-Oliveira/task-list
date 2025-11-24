package com.lmlasmo.tasklist.advice.exception;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MethodNotAllowedException;

import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;
import com.lmlasmo.tasklist.exception.OperationFailureException;
import com.lmlasmo.tasklist.exception.PreconditionFailedException;
import com.lmlasmo.tasklist.exception.ResourceAlreadyExistsException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;

import reactor.core.publisher.Mono;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MainAdviceController {
	
	@ExceptionHandler({
		NoResourceFoundException.class,
		ResourceNotFoundException.class
	})
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public Mono<Map<String, Object>> exceptionNotFoundResponse(Exception exception, ServerHttpRequest req){
		return Mono.just(AdviceWrapper.wrapper(HttpStatus.NOT_FOUND, exception.getMessage(), req));
	}
	
	@ExceptionHandler({
		ResourceAlreadyExistsException.class,
		OptimisticLockingFailureException.class
	})
	@ResponseStatus(code = HttpStatus.CONFLICT)
	public Mono<Map<String, Object>> exceptionConflitResponse(Exception exception, ServerHttpRequest req){		
		return Mono.just(AdviceWrapper.wrapper(HttpStatus.CONFLICT, exception.getMessage(), req));
	}	
	
	@ExceptionHandler(PreconditionFailedException.class)
	@ResponseStatus(code = HttpStatus.PRECONDITION_FAILED)
	public Mono<Map<String, Object>> exceptionResponse(PreconditionFailedException ex, ServerHttpRequest req) {
		return Mono.just(AdviceWrapper.wrapper(HttpStatus.PRECONDITION_FAILED, "ETag does not match current resource version", req));
	}
	
	@ExceptionHandler(MethodNotAllowedException.class)
	@ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
	public Mono<Map<String, Object>> exceptionResponse(MethodNotAllowedException ex, ServerHttpRequest req){
		String message = "HTTP method not supported. Supported methods %s";
		
		String[] methods = ex.getSupportedMethods().stream()
				.map(HttpMethod::name)
				.toArray(String[]::new);
		
		message = String.format(message, String.join(", ", methods));
		
		return Mono.just(AdviceWrapper.wrapper(HttpStatus.METHOD_NOT_ALLOWED,message, req));
	}
	
	@ExceptionHandler(OperationFailureException.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public Mono<Map<String, Object>> exceptionResponse(OperationFailureException ex, ServerHttpRequest req){
		return Mono.just(AdviceWrapper.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req));
	}
	
}
