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
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.StatusSummary;

public class SubtaskRepositoryTest extends AbstractSubtaskRepositoryTest {
	
	@Test
	void findPositionSummaryByTaskId() {
		getTasks().forEach(t -> {
			List<PositionSummary> idPositions = getSubtaskRepository().findPositionSummaryByTaskId(t.getId())
					.collectList().block();

			getSubtasks().stream()
				.filter(s -> s.getTaskId() == t.getId())
				.forEach(s -> {
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
		
		PositionSummary positionSummary = getSubtaskRepository().findPositionSummaryById(subtask.getId()).block();
		
		assertNotNull(positionSummary);
		assertEquals(subtask.getId(), positionSummary.getId());
		assertEquals(subtask.getPosition(), positionSummary.getPosition());
	}
	
	@Test
	void findPositionSummaryByRelatedSubtaskId() {
		int taskIndex = new Random().nextInt(0, getTasks().size());		
		Task task = getTasks().get(taskIndex);
		
		List<Subtask> subtasks = getSubtasks().stream()
				.filter(s -> s.getTaskId() == task.getId())
				.collect(Collectors.toList());
		
		int subtaskIndex = new Random().nextInt(0, subtasks.size());		
		Subtask subtask = subtasks.remove(subtaskIndex);
		
		List<PositionSummary> related = getSubtaskRepository().findPositionSummaryByRelatedSubtaskId(subtask.getId())
				.collectList()
				.block();
		
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

		getSubtaskRepository().deleteAllById(ids);
	}
	
	@RepeatedTest(10)
	void updatePriority() {
		int taskIndex = new Random().nextInt(0, getTasks().size());		
		Task task = getTasks().get(taskIndex);
		
		List<Subtask> sortedSubtasks = getSubtasks().stream()
				.filter(s -> s.getTaskId() == task.getId())
				.collect(Collectors.toList());
		
		sortedSubtasks.sort(Comparator.comparingInt(Subtask::getPosition));
		
		int subtaskIndex = new Random().nextInt(0, sortedSubtasks.size());
		Subtask subtask = sortedSubtasks.get(subtaskIndex);
		final int position = subtask.getPosition();
		
		Subtask lastSubtask = sortedSubtasks.get(subtaskIndex == sortedSubtasks.size()-1 ? 0 : sortedSubtasks.size()-1);
		final int lastPosition = lastSubtask.getPosition();
		
		getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), subtask.getVersion()), lastPosition > position ? lastPosition+1 : position+1).block();
		
		Subtask upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
		Subtask upLastSubtask = getSubtaskRepository().findById(lastSubtask.getId()).block();
		
		subtask.setPosition(upSubtask.getPosition());
		subtask.setVersion(upSubtask.getVersion());
		
		lastSubtask.setPosition(upLastSubtask.getPosition());
		lastSubtask.setVersion(upLastSubtask.getVersion());
		
		assertEquals(subtask.getPosition(), lastPosition > position ? lastPosition+1 : position+1);
		
		getSubtaskRepository().updatePriority(new BasicSummary(lastSubtask.getId(), lastSubtask.getVersion()), position).block();
		
		upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
		upLastSubtask = getSubtaskRepository().findById(lastSubtask.getId()).block();
		
		subtask.setPosition(upSubtask.getPosition());
		subtask.setVersion(upSubtask.getVersion());
		
		lastSubtask.setPosition(upLastSubtask.getPosition());
		lastSubtask.setVersion(upLastSubtask.getVersion());
		
		assertEquals(lastSubtask.getPosition(), position);
		
		assertThrows(DataIntegrityViolationException.class, () -> getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), subtask.getVersion()), lastSubtask.getPosition()).block());
	}
	
	@Test
	void updatePriorityWithVersion() {
	    Task task = getTasks().get(0);
	    List<Subtask> subtasks = getSubtasks().stream()
	    		.filter(s -> s.getTaskId() == task.getId())
	    		.toList();
	    
	    Subtask subtask = subtasks.get(0);

	    long initialVersion = subtask.getVersion();
	    int newPosition = subtask.getPosition() + subtasks.size() + 1;
 
	    getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), initialVersion), newPosition).block();

	    Subtask upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
	    
	    subtask.setPosition(upSubtask.getPosition());
	    subtask.setVersion(upSubtask.getVersion());

	    assertEquals(initialVersion + 1, subtask.getVersion());
	    assertEquals(newPosition, subtask.getPosition());

	    assertThrows(OptimisticLockingFailureException.class, 
	    		() -> getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), initialVersion), newPosition + 1).block()
	    );

	    getSubtaskRepository().updatePriority(new BasicSummary(subtask.getId(), subtask.getVersion()), newPosition + 2).block();

	    upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
	    
	    subtask.setPosition(upSubtask.getPosition());
	    subtask.setVersion(upSubtask.getVersion());
	    
	    assertEquals(initialVersion+2, subtask.getVersion());
	    assertEquals(newPosition + 2, subtask.getPosition());
	}
	
	@Test
	void findStatusSummaryById() {
		int index = new Random().nextInt(0, getSubtasks().size());		
		Subtask subtask = getSubtasks().get(index);
		
		StatusSummary statusSummary = getSubtaskRepository().findStatusSummaryById(subtask.getId()).block();
		
		assertNotNull(statusSummary);
		assertEquals(subtask.getId(), statusSummary.getId());
		assertEquals(subtask.getStatus(), statusSummary.getStatus());
	}
	
	@Test
	void findStatusSummaryByIds() {		
		List<Integer> ids = getSubtasks().stream()
				.map(Subtask::getId)
				.collect(Collectors.toList()); 
		
		List<StatusSummary> statusSummaries = getSubtaskRepository().findStatusSummaryByIds(ids)
				.collectList().block();
		
		assertEquals(
				statusSummaries.stream()
					.map(StatusSummary::getId)
					.collect(Collectors.toList()),
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
		
		getSubtaskRepository().updateStatus(new BasicSummary(subtask.getId(), subtask.getVersion()), TaskStatusType.COMPLETED).block();
		
		Subtask upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
		
		subtask.setStatus(upSubtask.getStatus());
		subtask.setVersion(upSubtask.getVersion());
		
		ids = new ArrayList<>(ids);
		ids.set(index, new BasicSummary(subtask.getId(), subtask.getVersion()));
		
		assertEquals(subtask.getStatus(), TaskStatusType.COMPLETED);
		
		getSubtaskRepository().updateStatus(ids, TaskStatusType.COMPLETED).block();
		
		upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
		
		subtask.setStatus(upSubtask.getStatus());
		subtask.setVersion(upSubtask.getVersion());
		
		
		long count = getSubtaskRepository().findAll().collectList().block().stream()
				.filter(s -> s.getStatus().equals(TaskStatusType.COMPLETED))
				.count();
		
		assertEquals(count, getSubtasks().size());
	}
	
	@Test
	void updateStatusWithVersion() {
	    int index = new Random().nextInt(0, getSubtasks().size());
	    Subtask subtask = getSubtasks().get(index);
	    	    
	    getSubtaskRepository().updateStatus(new BasicSummary(subtask.getId(), subtask.getVersion()), TaskStatusType.COMPLETED).block();

	    Subtask upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
	    
	    subtask.setStatus(upSubtask.getStatus());
	    subtask.setVersion(upSubtask.getVersion());
	    
	    assertEquals(TaskStatusType.COMPLETED, subtask.getStatus());	    
	    assertThrows(OptimisticLockingFailureException.class, 
	    		() -> getSubtaskRepository().updateStatus(new BasicSummary(subtask.getId(), subtask.getVersion() - 1), TaskStatusType.COMPLETED).block()
	    		);
	    
	    upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
	    
	    subtask.setStatus(upSubtask.getStatus());
	    subtask.setVersion(upSubtask.getVersion());
	    
	    assertEquals(TaskStatusType.COMPLETED, subtask.getStatus());

	    List<BasicSummary> basics = getSubtasks().stream()
	            .map(s -> new BasicSummary(s.getId(), s.getVersion()))
	            .toList();

	    assertDoesNotThrow(() -> getSubtaskRepository().updateStatus(basics, TaskStatusType.COMPLETED).block());
	}
	
	@Test
	void sumVersion() {
		getTasks().forEach(t -> {
			
			List<Subtask> subtasks = getSubtasks().stream()
					.filter(s -> t.getId().equals(s.getTaskId()))
					.toList();
			
			List<Integer> ids = subtasks.stream()
					.map(Subtask::getId)
					.toList();
			
			long sumByTask = subtasks.stream()
					.map(Subtask::getVersion)
					.reduce(Long::sum)
					.orElse(0L);
			
			assertEquals(sumByTask, getSubtaskRepository().sumVersionByTask(t.getId()).block());
			assertEquals(sumByTask, getSubtaskRepository().sumVersionByids(ids).block());
		});
	}


}
