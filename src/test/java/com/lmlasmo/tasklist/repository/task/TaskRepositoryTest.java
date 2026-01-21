package com.lmlasmo.tasklist.repository.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;

import com.lmlasmo.tasklist.mapper.summary.TaskSummaryMapper;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

public class TaskRepositoryTest extends AbstractTaskRepositoryTest{
	
	@Autowired
	private TaskSummaryMapper mapper;

	@Test
	void delete() {
		getTasks().forEach(t -> {
			getTaskRepository().deleteById(t.getId());
		});
	}
	
	@Test
	void findSummaryBy() {
		getTasks().forEach(t -> {
			StatusSummary summary = getTaskRepository().findStatusSummaryById(t.getId()).block();
			
			assertEquals(summary.getId(), t.getId());
			assertEquals(summary.getStatus(), t.getStatus());
			assertEquals(summary.getVersion(), t.getVersion());		
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
	void findByUserWithFilter() {
		getUsers().forEach(u -> {
			Pageable pageable = Pageable.ofSize(50);
			
			List<Task> tasks = getTaskRepository().findAllByUserId(u.getId(), pageable, "ID", TaskStatusType.PENDING)
					.collectList().block();

			assertTrue(tasks.size() == getMaxTasksPerUser() || tasks.size() == 50);
		});
	}

	@Test
	void updateStatus() {
		getTasks().forEach(t -> {
			for(TaskStatusType status: TaskStatusType.values()) {
				getTaskRepository().updateStatus(mapper.toStatusSummary(t.getId(), t.getVersion(), t.getStatus()), status).block();

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
	        final Task tt = t;
	        
	        getTaskRepository().updateStatus(new BasicSummary() {
				@Override public int getId() {return tt.getId();}
				
				@Override public long getVersion() {return tt.getVersion();}
			}, TaskStatusType.COMPLETED).block();
	        
	        Task task = getTaskRepository().findById(t.getId()).block();

	        assertEquals(TaskStatusType.COMPLETED, task.getStatus());
	        assertTrue(task.getVersion() > initialVersion);

	        assertThrows(OptimisticLockingFailureException.class, 
	        		() -> getTaskRepository().updateStatus(new BasicSummary() {
	        					@Override public int getId() {return tt.getId();}
	        					
	        					@Override public long getVersion() {return tt.getVersion();}
	    					}, TaskStatusType.IN_PROGRESS).block()
	        		);
	        
	        assertEquals(TaskStatusType.COMPLETED, task.getStatus());
	    });
	}

	@Test
	void sumByVersion() {
		getUsers().forEach(u -> {
			
			List<Task> tasks = getTasks()
					.stream()
					.filter(t -> u.getId().equals(t.getUserId()))
					.toList();
			
			List<Integer> ids = tasks.stream()
					.map(Task::getId)
					.toList();
			
			long sumByUser = tasks.stream()
					.map(Task::getVersion)
					.reduce(Long::sum)
					.orElse(0L);
			
			assertEquals(sumByUser, getTaskRepository().sumVersionByUser(u.getId()).block());
			assertEquals(sumByUser, getTaskRepository().sumVersionByids(ids).block());
		});
	}
	
	@Test
	void sumByVersionWithFilter() {
		getUsers().forEach(u -> {
			
			List<Task> tasks = getTasks()
					.stream()
					.filter(t -> u.getId().equals(t.getUserId()))
					.toList();
			
			long sumByUser = tasks.stream()
					.map(Task::getVersion)
					.reduce(Long::sum)
					.orElse(0L);
			
			assertEquals(
					sumByUser, 
					getTaskRepository().sumVersionByUser(
							u.getId(),
							Pageable.ofSize(50),
							"ID",
							TaskStatusType.PENDING).block());
		});
	}

}
