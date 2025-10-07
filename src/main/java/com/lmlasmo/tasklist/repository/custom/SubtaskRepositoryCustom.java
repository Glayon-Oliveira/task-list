package com.lmlasmo.tasklist.repository.custom;

import java.util.List;
import java.util.Optional;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;

public interface SubtaskRepositoryCustom {
		
	public List<SubtaskSummary.PositionSummary> findPositionSummaryByTaskId(int taskId);
		
	public Optional<SubtaskSummary.PositionSummary> findPositionSummaryById(int subtaskId);
		
	public List<PositionSummary> findPositionSummaryByRelatedSubtaskId(int subtaskId);
	
	public void updatePriority(int subtaskId, int position);
	
	public Optional<SubtaskSummary.StatusSummary> findStatusSummaryById(int subtaskId);
	
	public List<SubtaskSummary.StatusSummary> findStatusSummaryByIds(Iterable<Integer> subtaskIds);
	
	public void updateStatus(int subtaskId, TaskStatusType status);
	
	public void updateStatus(Iterable<Integer> subtaskIds, TaskStatusType status);
	
}
