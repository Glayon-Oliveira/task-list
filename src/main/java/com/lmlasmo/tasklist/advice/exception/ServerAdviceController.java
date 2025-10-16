package com.lmlasmo.tasklist.advice.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ServerAdviceController {
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, Object> exceptionResponse(Exception ex, HttpServletRequest req){
		ex.printStackTrace();
		return AdviceWrapper.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, req);
	}	
	
}
