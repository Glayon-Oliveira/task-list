package com.lmlasmo.tasklist.repository.custom;

import java.util.List;
import java.util.Optional;

import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;

public interface SubtaskRepositoryCustom {
		
	public List<SubtaskSummary.PositionSummary> findPositionSummaryByTaskId(int taskId);
		
	public Optional<SubtaskSummary.PositionSummary> findPositionSummaryById(int subtaskId);
		
	public List<PositionSummary> findPositionSummaryByRelatedSubtaskId(int subtaskId);
	
	public void updatePriority(BasicSummary basic, int position);
	
	public Optional<SubtaskSummary.StatusSummary> findStatusSummaryById(int subtaskId);
	
	public List<SubtaskSummary.StatusSummary> findStatusSummaryByIds(Iterable<Integer> subtaskIds);
	
	public void updateStatus(BasicSummary basic, TaskStatusType status);
	
	public void updateStatus(Iterable<? extends BasicSummary> basics, TaskStatusType status);
	
	public long sumVersionByids(Iterable<Integer> ids);
	
	public long sumVersionByTask(int taskId);
	
}
