package com.lmlasmo.tasklist.repository.summary;

import com.lmlasmo.tasklist.model.TaskStatusType;

public interface TaskSummary {
	
	public static interface StatusSummary extends BasicSummary {
		
		public TaskStatusType getStatus();
		
	}
	
}
