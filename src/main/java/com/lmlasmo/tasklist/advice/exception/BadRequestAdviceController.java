package com.lmlasmo.tasklist.advice.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;
import com.lmlasmo.tasklist.exception.EntityNotUpdateException;
import com.lmlasmo.tasklist.exception.InvalidEmailCodeException;
import com.lmlasmo.tasklist.exception.TaskHasSubtasksException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestAdviceController {
	
	@ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
	public Map<String, Object> exceptionResponse(UnsatisfiedServletRequestParameterException exception, HttpServletRequest request) {
		return AdviceWrapper.wrapper(exception.getMessage(), request);
	}
	
	@ExceptionHandler(EntityNotUpdateException.class)		
	public Map<String, Object> exceptionResponse(EntityNotUpdateException exception, HttpServletRequest request) {
		return AdviceWrapper.wrapper(exception.getMessage(), request);
	}
	
	@ExceptionHandler(TaskHasSubtasksException.class)		
	public Map<String, Object> exceptionResponse(TaskHasSubtasksException exception, HttpServletRequest request) {
		return AdviceWrapper.wrapper(exception.getMessage(), request);
	}
	
	@ExceptionHandler(BindException.class)	
	public Map<String, Object> exceptionResponse(BindException ex, HttpServletRequest req){		
		Map<String, Object> errors = new LinkedHashMap<>();
		
		ex.getFieldErrors().forEach(f -> {
			Map<String, Object> fieldValue = new LinkedHashMap<>();
			fieldValue.put("message", f.getDefaultMessage());
			fieldValue.put("rejectedValue", f.getRejectedValue());
			
			errors.put(f.getField(), fieldValue);
		});
		
		return AdviceWrapper.wrapper(req, Map.of("fieldErrors", errors));
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public Map<String, Object> exceptionResponse(ConstraintViolationException ex, HttpServletRequest req){
		Map<String, Object> constraints = new LinkedHashMap<>();
		
		ex.getConstraintViolations().forEach(c -> {
			Map<String, Object> fieldValue = new LinkedHashMap<>();
			fieldValue.put("message", c.getMessage());
			fieldValue.put("rejectedValue", c.getInvalidValue());
			
			constraints.put(c.getPropertyPath().toString(), fieldValue);
		});
		
		return AdviceWrapper.wrapper(ex.getMessage(), req, Map.of("fieldErrors", constraints));
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Map<String, Object> exceptionResponse(HttpMessageNotReadableException ex, HttpServletRequest req){
		String message = "Request body is invalid or malformed";
		return AdviceWrapper.wrapper(message, req);
	}
	
	@ExceptionHandler(MissingServletRequestParameterException.class)	
	public Map<String, Object> exceptionResponse(MissingServletRequestParameterException ex, HttpServletRequest req){
		return AdviceWrapper.wrapper(ex.getMessage(), req);
	}
	
	@ExceptionHandler(TypeMismatchException.class)	
	public Map<String, Object> exceptionResponse(TypeMismatchException ex, HttpServletRequest req){
		return AdviceWrapper.wrapper(ex.getMessage(), req);
	}
	
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public Map<String, Object> exceptionResponse(HttpMediaTypeNotSupportedException ex, HttpServletRequest req){
		return AdviceWrapper.wrapper(ex.getMessage(), req);
	}
	
	@ExceptionHandler(InvalidEmailCodeException.class)
	public Map<String, Object> exceptionResponse(InvalidEmailCodeException ex, HttpServletRequest req) {
		return AdviceWrapper.wrapper(ex.getMessage(), req);
	}

}
