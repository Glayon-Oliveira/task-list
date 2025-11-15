package com.lmlasmo.tasklist.repository.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;

public class TaskRepositoryTest extends AbstractTaskRepositoryTest{

	@Test
	void delete() {
		getTasks().forEach(t -> {
			getTaskRepository().deleteById(t.getId());
		});
	}

	@Test
	void findByUser() {
		getUsers().forEach(u -> {
			List<Task> tasks = getTaskRepository().findByUserId(u.getId()).collectList().block();

			assertTrue(tasks.size() == getMaxTasksPerUser());
		});
	}

	@Test
	void updateStatus() {
		getTasks().forEach(t -> {
			for(TaskStatusType status: TaskStatusType.values()) {
				getTaskRepository().updateStatus(new BasicSummary(t.getId(), t.getVersion()), status).block();

				t = getTaskRepository().findById(t.getId()).block();

				assertNotNull(t);
				assertTrue(t.getStatus().equals(status));
			}
		});
	}
	
	@Test
	void updateStatusWithVersion() {
	    getTasks().forEach(t -> {
	    	
	        long initialVersion = t.getVersion();
	        
	        getTaskRepository().updateStatus(new BasicSummary(t.getId(), t.getVersion()), TaskStatusType.COMPLETED).block();
	        
	        Task task = getTaskRepository().findById(t.getId()).block();

	        assertEquals(TaskStatusType.COMPLETED, task.getStatus());
	        assertTrue(task.getVersion() > initialVersion);

	        assertThrows(OptimisticLockingFailureException.class, 
	        		() -> getTaskRepository().updateStatus(new BasicSummary(t.getId(), initialVersion), TaskStatusType.IN_PROGRESS).block()
	        		);
	        
	        assertEquals(TaskStatusType.COMPLETED, task.getStatus());
	    });
	}


}
