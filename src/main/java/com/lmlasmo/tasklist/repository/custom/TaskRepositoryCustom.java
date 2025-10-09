package com.lmlasmo.tasklist.repository.custom;

import java.util.Optional;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

public interface TaskRepositoryCustom {
	
	public Optional<StatusSummary> findStatusSummaryById(int taskId);

	public void updateStatus(BasicSummary basic, TaskStatusType status);
	
}
