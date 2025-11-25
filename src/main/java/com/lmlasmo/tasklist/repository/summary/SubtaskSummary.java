package com.lmlasmo.tasklist.repository.summary;

import java.math.BigDecimal;

import com.lmlasmo.tasklist.model.TaskStatusType;

public interface SubtaskSummary {
		
	public static interface BasicSubtaskSummary extends BasicSummary {
		
		public int getTaskId();
		
	}
		
	public static interface PositionSummary extends BasicSubtaskSummary {
		
		public BigDecimal getPosition();
	}
		
	public static interface StatusSummary extends BasicSubtaskSummary{
		
		public TaskStatusType getStatus();
		public  int getTaskId();
		
	}

}
