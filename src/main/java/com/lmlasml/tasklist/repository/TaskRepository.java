package com.lmlasml.tasklist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lmlasml.tasklist.model.Task;

public interface TaskRepository extends JpaRepository<Task, Integer>{

	public Page<Task> findByUserId(int id, Pageable pageable);
	
}
