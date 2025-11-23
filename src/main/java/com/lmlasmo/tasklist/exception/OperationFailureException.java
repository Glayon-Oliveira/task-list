package com.lmlasmo.tasklist.exception;

public class OperationFailureException extends RuntimeException {

	private static final long serialVersionUID = -5311169387192426252L;

	public OperationFailureException(String message) {
		super(message);
	}
	
}
