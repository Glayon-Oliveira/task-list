package com.lmlasmo.tasklist.exception;

import jakarta.persistence.EntityExistsException;

public class EntityNotDeleteException extends EntityExistsException{

	private static final long serialVersionUID = -1608043056437326617L;

	public EntityNotDeleteException(String message) {
		super(message);
	}
	
}
