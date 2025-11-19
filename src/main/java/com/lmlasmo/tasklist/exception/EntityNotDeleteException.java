package com.lmlasmo.tasklist.exception;

public class EntityNotDeleteException extends RuntimeException {

	private static final long serialVersionUID = -1608043056437326617L;

	public EntityNotDeleteException(String message) {
		super(message);
	}
	
}
