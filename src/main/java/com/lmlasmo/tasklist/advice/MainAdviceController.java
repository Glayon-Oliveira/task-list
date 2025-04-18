package com.lmlasmo.tasklist.advice;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.lmlasmo.tasklist.advice.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.advice.exception.EntityNotUpdateException;
import com.lmlasmo.tasklist.advice.exception.ExceptionResponseDTO;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class MainAdviceController {
	
	@ExceptionHandler(UsernameNotFoundException.class)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
	public ExceptionResponseDTO usernameNotFoundException(UsernameNotFoundException exception, HttpServletRequest request) {
		return new ExceptionResponseDTO(401, HttpStatus.UNAUTHORIZED.getReasonPhrase(),exception.getMessage(),
				request.getServletPath(), LocalDateTime.now());
	}
	
	@ExceptionHandler(JWTVerificationException.class)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
	public ExceptionResponseDTO jwtVerifier(JWTVerificationException exception, HttpServletRequest request) {
		return new ExceptionResponseDTO(401, HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Invalid token",
				request.getServletPath(), LocalDateTime.now());
	}	

	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public ExceptionResponseDTO notFound(EntityNotFoundException exception, HttpServletRequest request){
		return new ExceptionResponseDTO(404, HttpStatus.NOT_FOUND.getReasonPhrase(),exception.getMessage(),
				request.getServletPath(), LocalDateTime.now());
	}
	
	@ExceptionHandler(EntityExistsException.class)	
	public ResponseEntity<ExceptionResponseDTO> EntityExistsException(EntityExistsException exception, HttpServletRequest request){		
		
		ExceptionResponseDTO response = new ExceptionResponseDTO(400, HttpStatus.BAD_REQUEST.getReasonPhrase(),
				exception.getMessage(), request.getServletPath(), LocalDateTime.now());
		
		if(exception instanceof EntityNotDeleteException) {
			response.setStatus(500);
			response.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
			return ResponseEntity.internalServerError().body(response);
		}else {
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	@ExceptionHandler(EntityNotUpdateException.class)
	@ResponseBody
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	public ExceptionResponseDTO exceptionResponseDTO(EntityNotUpdateException exception, HttpServletRequest request) {
		return new ExceptionResponseDTO(400, HttpStatus.BAD_REQUEST.getReasonPhrase(), exception.getMessage(),
				request.getServletPath(), LocalDateTime.now());
	}
	
}
