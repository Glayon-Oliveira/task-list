package com.lmlasmo.tasklist.exception;

public class InvalidEmailCodeException extends RuntimeException {

	private static final long serialVersionUID = 6159376172812185320L;
	
	public InvalidEmailCodeException(String message) {
		super(message);
	}

}
