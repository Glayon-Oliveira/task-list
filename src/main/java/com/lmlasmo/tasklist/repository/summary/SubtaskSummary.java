package com.lmlasmo.tasklist.repository.summary;

import java.math.BigDecimal;

import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;

public interface SubtaskSummary {
	
	@Getter
	public static class BasicSubtaskSummary extends BasicSummary {
		private final int taskId;
		
		
		public BasicSubtaskSummary(int id, long version, int taskId) {
			super(id, version);
			this.taskId = taskId;
		}
	}
	
	@Getter
	public static class PositionSummary extends BasicSubtaskSummary {
		private final BigDecimal position;
		
		public PositionSummary(int id, long version, BigDecimal position, int taskId) {
			super(id, version, taskId);
			this.position = position;
		}
	}
	
	@Getter	
	public static class StatusSummary extends BasicSubtaskSummary{		
		private final TaskStatusType status;
		private final int taskId;
		
		public StatusSummary(int id, long version, TaskStatusType status, int taskId) {
			super(id, version, taskId);
			this.status = status;
			this.taskId = taskId;
		}
	}

}
