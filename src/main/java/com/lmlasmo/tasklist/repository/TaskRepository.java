package com.lmlasmo.tasklist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.repository.custom.TaskRepositoryCustom;

public interface TaskRepository extends JpaRepository<Task, Integer>, TaskRepositoryCustom {

	public Page<Task> findByUserId(int id, Pageable pageable);

	public boolean existsByIdAndUserId(int taskId, int userId);

	public boolean existsByIdAndVersion(int id, long version);
	
}
