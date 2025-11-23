package com.lmlasmo.tasklist.exception;

public class ResourceNotDeletableException extends OperationFailureException {

	private static final long serialVersionUID = -1608043056437326617L;

	public ResourceNotDeletableException(String message) {
		super(message);
	}
	
}
