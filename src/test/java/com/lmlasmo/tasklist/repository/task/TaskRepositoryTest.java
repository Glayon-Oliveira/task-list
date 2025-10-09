package com.lmlasmo.tasklist.repository.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;

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
				getTaskRepository().updateStatus(new BasicSummary(t.getId(), t.getVersion()), status);

				Task task = getTaskRepository().findById(t.getId()).orElse(null);
				em.refresh(task);

				assertNotNull(task);
				assertTrue(task.getStatus().equals(status));
			}
		});
	}
	
	@Test
	void updateStatusWithVersion() {
	    getTasks().forEach(t -> {
	        em.refresh(t);
	        long initialVersion = t.getVersion();
	        
	        getTaskRepository().updateStatus(new BasicSummary(t.getId(), t.getVersion()), TaskStatusType.COMPLETED);
	        em.refresh(t);	        

	        assertEquals(TaskStatusType.COMPLETED, t.getStatus());
	        assertTrue(t.getVersion() > initialVersion);

	        assertThrows(JpaOptimisticLockingFailureException.class, 
	        		() -> getTaskRepository().updateStatus(new BasicSummary(t.getId(), initialVersion), TaskStatusType.IN_PROGRESS)
	        		);

	        em.refresh(t);
	        assertEquals(TaskStatusType.COMPLETED, t.getStatus());
	    });
	}


}
