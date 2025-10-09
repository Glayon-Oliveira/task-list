package com.lmlasmo.tasklist.repository.subtask;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.StatusSummary;

import jakarta.persistence.EntityManager;

public class SubtaskRepositoryTest extends AbstractSubtaskRepositoryTest {
	
	@Autowired
	private EntityManager entityManager;
	
	@Test
	void findPositionSummaryByTaskId() {
		getTasks().forEach(t -> {
			List<PositionSummary> idPositions = getSubtaskRepository().findPositionSummaryByTaskId(t.getId());

			t.getSubtasks().forEach(s -> {
				PositionSummary idPosition = idPositions.stream()
						.filter(ip -> ip.getId() == s.getId())
						.findFirst().orElseThrow();

				assertTrue(idPosition.getId() == s.getId());
				assertTrue(idPosition.getPosition() == s.getPosition());
			});
		});
	}
	
	@Test
	void findPositionSummaryById() {
		int index = new Random().nextInt(0, getSubtasks().size());		
		Subtask subtask = getSubtasks().get(index);
		
		PositionSummary positionSummary = getSubtaskRepository().findPositionSummaryById(subtask.getId())
				.orElseGet(() -> null);
		
		assertNotNull(positionSummary);
		assertEquals(subtask.getId(), positionSummary.getId());
		assertEquals(subtask.getPosition(), positionSummary.getPosition());
	}
	
	@Test
	void findPositionSummaryByRelatedSubtaskId() {
		int taskIndex = new Random().nextInt(0, getTasks().size());		
		Task task = getTasks().get(taskIndex);
		
		List<Subtask> subtasks = new ArrayList<>(task.getSubtasks());
		
		int subtaskIndex = new Random().nextInt(0, subtasks.size());		
		Subtask subtask = subtasks.remove(subtaskIndex);
		
		List<PositionSummary> related = getSubtaskRepository().findPositionSummaryByRelatedSubtaskId(subtask.getId());
		
		assertEquals(
				related.stream()
				 	.map(PositionSummary::getId)
				 	.collect(Collectors.toSet()),
				subtasks.stream()
				 	.map(Subtask::getId)
				 	.collect(Collectors.toSet())
				);
		
		assertEquals(
				related.stream()
					.map(PositionSummary::getPosition)
					.collect(Collectors.toSet()),
				subtasks.stream()
				 	.map(Subtask::getPosition)
				 	.collect(Collectors.toSet())
				);
	}

	@Test
	void delete() {
		List<Integer> ids = getSubtasks().stream()
				.map(Subtask::getId)
				.toList();

		getSubtaskRepository().deleteAllByIdInBatch(ids);
	}
	
	@RepeatedTest(10)
	void updatePriority() {
		int taskIndex = new Random().nextInt(0, getTasks().size());		
		Task task = getTasks().get(taskIndex);
		
		List<Subtask> sortedSubtasks = new ArrayList<>(task.getSubtasks());
		sortedSubtasks.sort(Comparator.comparingInt(Subtask::getPosition));
		
		int subtaskIndex = new Random().nextInt(0, sortedSubtasks.size());
		Subtask subtask = sortedSubtasks.get(subtaskIndex);
		final int position = subtask.getPosition();
		
		Subtask lastSubtask = sortedSubtasks.get(subtaskIndex == sortedSubtasks.size()-1 ? 0 : sortedSubtasks.size()-1);
		final int lastPosition = lastSubtask.getPosition();
		
		getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), subtask.getVersion()), lastPosition > position ? lastPosition+1 : position+1);
		entityManager.refresh(subtask);		
		assertEquals(subtask.getPosition(), lastPosition > position ? lastPosition+1 : position+1);
		
		getSubtaskRepository().updatePriority(new BasicSummary(lastSubtask.getId(), lastSubtask.getVersion()), position);
		getSubtaskRepository().flush();
		entityManager.refresh(lastSubtask);
		assertEquals(lastSubtask.getPosition(), position);
		
		assertThrows(DataIntegrityViolationException.class, () -> getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), subtask.getVersion()), position));						
	}
	
	@Test
	void updatePriorityWithVersion() {
	    Task task = getTasks().get(0);
	    Subtask subtask = task.getSubtasks().stream().findFirst().orElseThrow();

	    long initialVersion = subtask.getVersion();
	    int newPosition = subtask.getPosition() + task.getSubtasks().size() + 1;
 
	    getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), initialVersion), newPosition);

	    entityManager.refresh(subtask);

	    assertEquals(initialVersion + 1, subtask.getVersion());
	    assertEquals(newPosition, subtask.getPosition());

	    assertThrows(JpaOptimisticLockingFailureException.class, 
	    		() -> getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), initialVersion), newPosition + 1)
	    );

	    getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), subtask.getVersion()), newPosition + 2);

	    entityManager.refresh(subtask);
	    assertEquals(2, subtask.getVersion());
	    assertEquals(newPosition + 2, subtask.getPosition());
	}
	
	@Test
	void findStatusSummaryById() {
		int index = new Random().nextInt(0, getSubtasks().size());		
		Subtask subtask = getSubtasks().get(index);
		
		StatusSummary statusSummary = getSubtaskRepository().findStatusSummaryById(subtask.getId())
				.orElseGet(() -> null);
		
		assertNotNull(statusSummary);
		assertEquals(subtask.getId(), statusSummary.getId());
		assertEquals(subtask.getStatus(), statusSummary.getStatus());
	}
	
	@Test
	void findStatusSummaryByIds() {		
		Set<Integer> ids = getSubtasks().stream()
				.map(Subtask::getId)
				.collect(Collectors.toSet()); 
		
		List<StatusSummary> statusSummaries = getSubtaskRepository().findStatusSummaryByIds(ids);
		
		assertEquals(
				statusSummaries.stream()
					.map(StatusSummary::getId)
					.collect(Collectors.toSet()), 
				ids
				);
		
		assertEquals(
				statusSummaries.stream()
					.map(StatusSummary::getStatus)
					.collect(Collectors.toSet()), 
				getSubtasks().stream()
					.map(Subtask::getStatus)
					.collect(Collectors.toSet())
				);
	}
	
	@Test
	void updateStatus() {
		int index = new Random().nextInt(0, getSubtasks().size());
		Subtask subtask = getSubtasks().get(index);
		
		List<BasicSummary> ids = getSubtasks().stream()
				.map(s -> new BasicSummary(s.getId(), s.getVersion()))
				.toList();
		
		getSubtaskRepository().updateStatus(new BasicSummary(subtask.getId(), subtask.getVersion()), TaskStatusType.COMPLETED);
		entityManager.refresh(subtask);
		
		ids = new ArrayList<>(ids);
		ids.set(index, new BasicSummary(subtask.getId(), subtask.getVersion()));
		
		assertEquals(subtask.getStatus(), TaskStatusType.COMPLETED);
		
		getSubtaskRepository().updateStatus(ids, TaskStatusType.COMPLETED);		
		getSubtasks().forEach(s -> entityManager.refresh(s));
		
		long count = getSubtasks().stream().filter(s -> s.getStatus().equals(TaskStatusType.COMPLETED)).count();		
		assertEquals(count, getSubtasks().size());
	}
	
	@Test
	void updateStatusWithVersion() {
	    int index = new Random().nextInt(0, getSubtasks().size());
	    Subtask subtask = getSubtasks().get(index);
	    	    
	    getSubtaskRepository().updateStatus(new BasicSummary(subtask.getId(), subtask.getVersion()), TaskStatusType.COMPLETED);
	    entityManager.refresh(subtask);
	    
	    assertEquals(TaskStatusType.COMPLETED, subtask.getStatus());	    
	    assertThrows(JpaOptimisticLockingFailureException.class, 
	    		() -> getSubtaskRepository().updateStatus(new BasicSummary(subtask.getId(), subtask.getVersion() - 1), TaskStatusType.COMPLETED)
	    		);
	    
	    entityManager.refresh(subtask);
	    assertEquals(TaskStatusType.COMPLETED, subtask.getStatus());

	    List<BasicSummary> basics = getSubtasks().stream()
	            .map(s -> new BasicSummary(s.getId(), s.getVersion()))
	            .toList();

	    assertDoesNotThrow(() -> getSubtaskRepository().updateStatus(basics, TaskStatusType.COMPLETED));
	}


}
