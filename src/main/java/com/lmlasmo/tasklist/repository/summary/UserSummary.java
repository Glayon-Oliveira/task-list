package com.lmlasmo.tasklist.repository.summary;

import java.time.Instant;

import com.lmlasmo.tasklist.model.UserStatusType;

import lombok.Getter;

public interface UserSummary {

	@Getter
	public static class StatusSummary extends BasicSummary {
		
		private UserStatusType status;
		private Instant lastLogin;
		
		public StatusSummary(int id, long version, UserStatusType status, Instant lastLogin) {
			super(id, version);
			this.status = status;
			this.lastLogin = lastLogin;
		}
		
	}
	
}
