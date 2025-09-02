package com.lmlasmo.tasklist.advice;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.lmlasmo.tasklist.advice.util.AdviceWrapper;

import jakarta.servlet.http.HttpServletRequest;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SecurityAdviceController {

	@ExceptionHandler(exception = {AuthenticationException.class, JWTVerificationException.class})		
	@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
	public Map<String, Object> exceptionResponse(AuthenticationException exception, HttpServletRequest request) {
		return AdviceWrapper.wrapper(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);		
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(code = HttpStatus.FORBIDDEN)
	public Map<String, Object> exceptionResponse(AccessDeniedException ex, HttpServletRequest req){
		return AdviceWrapper.wrapper(HttpStatus.FORBIDDEN, ex.getMessage(), req);
	}
	
}
