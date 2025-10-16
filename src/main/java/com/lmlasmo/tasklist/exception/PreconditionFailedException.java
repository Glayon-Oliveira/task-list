package com.lmlasmo.tasklist.exception;

public class PreconditionFailedException extends RuntimeException {

	private static final long serialVersionUID = 4221884541060877019L;
	
	public PreconditionFailedException(String message) {
		super(message);
	}

}
