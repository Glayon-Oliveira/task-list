package com.lmlasmo.tasklist.repository.summary;

import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;

public interface TaskSummary {

	@Getter
	public static class StatusSummary extends BasicSummary {
		private TaskStatusType status;
		
		public StatusSummary(int id, long version, TaskStatusType status) {
			super(id, version);
			this.status = status;
		}
	}
	
}
