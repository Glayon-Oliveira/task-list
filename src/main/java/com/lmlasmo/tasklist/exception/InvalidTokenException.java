package com.lmlasmo.tasklist.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenException extends AuthenticationException {
	
    private static final long serialVersionUID = -669354109394841131L;

	public InvalidTokenException(String message) {
        super(message);
    }
	
}