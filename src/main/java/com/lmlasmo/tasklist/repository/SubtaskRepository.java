package com.lmlasmo.tasklist.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.custom.SubtaskRepositoryCustom;

public interface SubtaskRepository extends JpaRepository<Subtask, Integer>, SubtaskRepositoryCustom {

	public Page<Subtask> findByTaskId(int taskId, Pageable pageable);
	
	public boolean existsByTaskIdAndStatus(int taskId, TaskStatusType status);
	
	public boolean existsByIdAndTaskUserId(int subtaskId, int userId);	
		
	public long countByTaskId(int taskId);	

	public long countByIdInAndTaskUserId(List<Integer> subtaskIds, int userId);
	
}
