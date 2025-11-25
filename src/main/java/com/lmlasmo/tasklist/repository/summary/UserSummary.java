package com.lmlasmo.tasklist.repository.summary;

import java.time.Instant;

import com.lmlasmo.tasklist.model.UserStatusType;

public interface UserSummary {
	
	public static interface StatusSummary extends BasicSummary {
		
		public UserStatusType getStatus();
		
		public Instant getLastLogin();
		
	}
	
}
