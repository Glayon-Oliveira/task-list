package com.lmlasmo.tasklist.advice.exception;

public class EntityNotUpdateException extends RuntimeException{

	private static final long serialVersionUID = 3416237154049375682L;
	
	public EntityNotUpdateException(String message) {
		super(message);
	}

}
