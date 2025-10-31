package com.lmlasmo.tasklist.exception;

public class ExpiredTokenException extends InvalidTokenException {

	private static final long serialVersionUID = -8616919051897109711L;

	public ExpiredTokenException(String message) {
        super(message);
    }

}
