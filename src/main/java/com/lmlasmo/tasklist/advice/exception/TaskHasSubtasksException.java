package com.lmlasmo.tasklist.advice.exception;

public class TaskHasSubtasksException extends RuntimeException{

	private static final long serialVersionUID = 1722893524851274814L;
	
	public TaskHasSubtasksException(String message) {
		super(message);
	}

}
