package com.lmlasmo.tasklist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;

public interface TaskRepository extends JpaRepository<Task, Integer>{

	public Page<Task> findByUserId(int id, Pageable pageable);
	
	@Modifying
	@Transactional
	@Query("UPDATE Task t SET t.status = :status WHERE t.id = :taskId")
	public void updateStatus(int taskId, TaskStatusType status);
	
}
