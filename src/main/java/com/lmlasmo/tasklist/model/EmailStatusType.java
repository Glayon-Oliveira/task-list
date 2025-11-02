package com.lmlasmo.tasklist.model;

import java.util.Set;

public enum EmailStatusType {
	ACTIVE,
	SUSPENDED,
	INACTIVE;
	
	public static Set<EmailStatusType> getAllowedStatus() {
		return Set.of(ACTIVE);
	}
	
	public static Set<EmailStatusType> getLockedStatus() {
		return Set.of(SUSPENDED, INACTIVE);
	}
}
