package com.lmlasmo.tasklist.repository.summary;

import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;

public interface SubtaskSummary {	
	
	@Getter
	public static class PositionSummary extends BasicSummary{
		private final int position;
		
		public PositionSummary(int id, long version, int position) {
			super(id, version);
			this.position = position;
		}
	}
	
	@Getter	
	public static class StatusSummary extends BasicSummary{		
		private final TaskStatusType status;
		private final int taskId;
		
		public StatusSummary(int id, long version, TaskStatusType status, int taskId) {
			super(id, version);
			this.status = status;
			this.taskId = taskId;
		}
	}

}
