package com.lmlasmo.tasklist.advice.exception;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;
import com.lmlasmo.tasklist.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.exception.PreconditionFailedException;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class MainAdviceController {
	
	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public Map<String, Object> exceptionResponse(EntityNotFoundException exception, HttpServletRequest request){
		return AdviceWrapper.wrapper(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}
	
	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public Map<String, Object> exceptionResponse(NoResourceFoundException exception, HttpServletRequest request){
		return AdviceWrapper.wrapper(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}
	
	@ExceptionHandler(EntityExistsException.class)
	@ResponseStatus(code = HttpStatus.CONFLICT)
	public Map<String, Object> exceptionResponse(EntityExistsException exception, HttpServletRequest request){		
		return AdviceWrapper.wrapper(HttpStatus.CONFLICT, exception.getMessage(), request);
	}
	
	@ExceptionHandler(JpaOptimisticLockingFailureException.class)
	@ResponseStatus(code = HttpStatus.CONFLICT)
	public Map<String, Object> exceptionResponse(JpaOptimisticLockingFailureException exception, HttpServletRequest request) {
		return AdviceWrapper.wrapper(HttpStatus.CONFLICT, "The entity has been modified by another process. Please update the data and try again", request);
	}
	
	@ExceptionHandler(PreconditionFailedException.class)
	@ResponseStatus(code = HttpStatus.PRECONDITION_FAILED)
	public Map<String, Object> exceptionResponse(PreconditionFailedException ex, HttpServletRequest req) {
		return AdviceWrapper.wrapper(HttpStatus.PRECONDITION_FAILED, "ETag does not match current resource version", req);
	}
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
	public Map<String, Object> exceptionResponse(HttpRequestMethodNotSupportedException ex, HttpServletRequest req){
		String message = "HTTP method not supported. Supported methods %s";
		message = String.format(message, String.join(", ", ex.getSupportedMethods()));
		
		return AdviceWrapper.wrapper(HttpStatus.METHOD_NOT_ALLOWED,message, req);
	}	
	
	@ExceptionHandler(EntityNotDeleteException.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, Object> exceptionResponse(EntityNotDeleteException ex, HttpServletRequest req){
		return AdviceWrapper.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
	}
	
}
