package com.lmlasmo.tasklist.repository.summary;

import com.lmlasmo.tasklist.model.TaskStatusType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface SubtaskSummary {
	
	@Getter
	@RequiredArgsConstructor
	public static class PositionSummary{
		private final int id;
		private final int position;	
	}
	
	@Getter
	@RequiredArgsConstructor
	public static class StatusSummary{
		private final int id;
		private final TaskStatusType status;
		private final int taskId;
	}

}
