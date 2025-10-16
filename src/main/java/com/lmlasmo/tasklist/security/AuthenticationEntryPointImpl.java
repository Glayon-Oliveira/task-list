package com.lmlasmo.tasklist.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmlasmo.tasklist.advice.exception.util.AdviceWrapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
	
	private ObjectMapper mapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		
		Map<String, Object> exceptionResponse = AdviceWrapper.wrapper(HttpStatus.UNAUTHORIZED, authException.getMessage(), request);		
		String jsonResponse = mapper.writeValueAsString(exceptionResponse);
		
		response.setContentType(MediaType.APPLICATION_JSON.toString());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write(jsonResponse);		
	}

}
