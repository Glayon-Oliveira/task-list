package com.lmlasmo.tasklist.repository.custom;

import com.lmlasmo.tasklist.model.TaskStatusType;

public interface TaskRepositoryCustom {

	public void updateStatus(int taskId, TaskStatusType status);
	
}
