package com.lmlasmo.tasklist.repository.task;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;

import jakarta.persistence.EntityManager;

public class TaskRepositoryTest extends AbstractTaskRepositoryTest{

	@Autowired
	private EntityManager em;

	@Test
	void delete() {
		getTasks().forEach(t -> {
			getTaskRepository().deleteById(t.getId());
		});
	}

	@Test
	void findByUser() {
		getUsers().forEach(u -> {
			Pageable pageable = PageRequest.of(0, getMaxTasksPerUser());
			Page<Task> page = getTaskRepository().findByUserId(u.getId(), pageable);

			assertTrue(page.getSize() == getMaxTasksPerUser());
		});
	}

	@Test
	void updateStatus() {
		getTasks().forEach(t -> {
			for(TaskStatusType status: TaskStatusType.values()) {
				getTaskRepository().updateStatus(t.getId(), status);

				Task task = getTaskRepository().findById(t.getId()).orElse(null);
				em.refresh(task);

				assertNotNull(task);
				assertTrue(task.getStatus().equals(status));
			}
		});
	}

}
