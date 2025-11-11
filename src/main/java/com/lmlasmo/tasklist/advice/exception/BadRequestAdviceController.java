package com.lmlasmo.tasklist.advice.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;
import com.lmlasmo.tasklist.exception.EntityNotUpdateException;
import com.lmlasmo.tasklist.exception.InvalidEmailCodeException;
import com.lmlasmo.tasklist.exception.TaskHasSubtasksException;

import jakarta.validation.ConstraintViolationException;
import reactor.core.publisher.Mono;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestAdviceController {
	
	@ExceptionHandler(EntityNotUpdateException.class)		
	public Mono<Map<String, Object>> exceptionResponse(EntityNotUpdateException exception, ServerHttpRequest request) {
		return Mono.just(AdviceWrapper.wrapper(exception.getMessage(), request));
	}
	
	@ExceptionHandler(TaskHasSubtasksException.class)		
	public Mono<Map<String, Object>> exceptionResponse(TaskHasSubtasksException exception, ServerHttpRequest request) {
		return Mono.just(AdviceWrapper.wrapper(exception.getMessage(), request));
	}
	
	@ExceptionHandler(BindException.class)	
	public Mono<Map<String, Object>> exceptionResponse(BindException ex, ServerHttpRequest req){		
		Map<String, Object> errors = new LinkedHashMap<>();
		
		ex.getFieldErrors().forEach(f -> {
			Map<String, Object> fieldValue = new LinkedHashMap<>();
			fieldValue.put("message", f.getDefaultMessage());
			fieldValue.put("rejectedValue", f.getRejectedValue());
			
			errors.put(f.getField(), fieldValue);
		});
		
		return Mono.just(AdviceWrapper.wrapper(req, Map.of("fieldErrors", errors)));
	}
	
	@ExceptionHandler(WebExchangeBindException.class)
	public Mono<Map<String, Object>> exceptionResponse(WebExchangeBindException ex, ServerHttpRequest req) {
		Map<String, Object> errors = new LinkedHashMap<>();
		
		ex.getFieldErrors().forEach(f -> {
			Map<String, Object> fieldValue = new LinkedHashMap<>();
			fieldValue.put("message", f.getDefaultMessage());
			fieldValue.put("rejectedValue", f.getRejectedValue());
			
			errors.put(f.getField(), fieldValue);
		});
		
		return Mono.just(AdviceWrapper.wrapper(req, Map.of("fieldErrors", errors)));
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public Mono<Map<String, Object>> exceptionResponse(ConstraintViolationException ex, ServerHttpRequest req){
		Map<String, Object> constraints = new LinkedHashMap<>();
		
		ex.getConstraintViolations().forEach(c -> {
			Map<String, Object> fieldValue = new LinkedHashMap<>();
			fieldValue.put("message", c.getMessage());
			fieldValue.put("rejectedValue", c.getInvalidValue());
			
			constraints.put(c.getPropertyPath().toString(), fieldValue);
		});
		
		return Mono.just(AdviceWrapper.wrapper(ex.getMessage(), req, Map.of("fieldErrors", constraints)));
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Mono<Map<String, Object>> exceptionResponse(HttpMessageNotReadableException ex, ServerHttpRequest req){
		String message = "Request body is invalid or malformed";
		return Mono.just(AdviceWrapper.wrapper(message, req));
	}
	
	@ExceptionHandler(ServerWebInputException.class)	
	public Mono<Map<String, Object>> exceptionResponse(ServerWebInputException ex, ServerHttpRequest req){
		return Mono.just(AdviceWrapper.wrapper(ex.getMessage(), req));
	}
	
	@ExceptionHandler(TypeMismatchException.class)	
	public Mono<Map<String, Object>> exceptionResponse(TypeMismatchException ex, ServerHttpRequest req){
		return Mono.just(AdviceWrapper.wrapper(ex.getMessage(), req));
	}
	
	@ExceptionHandler(UnsupportedMediaTypeStatusException.class)
	public Mono<Map<String, Object>> exceptionResponse(UnsupportedMediaTypeStatusException ex, ServerHttpRequest req){
		return Mono.just(AdviceWrapper.wrapper(ex.getMessage(), req));
	}
	
	@ExceptionHandler(InvalidEmailCodeException.class)
	public Mono<Map<String, Object>> exceptionResponse(InvalidEmailCodeException ex, ServerHttpRequest req) {
		return Mono.just(AdviceWrapper.wrapper(ex.getMessage(), req));
	}

}
