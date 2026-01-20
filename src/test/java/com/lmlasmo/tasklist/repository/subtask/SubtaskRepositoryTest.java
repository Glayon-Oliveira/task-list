package com.lmlasmo.tasklist.repository.subtask;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;

import com.lmlasmo.tasklist.mapper.summary.SubtaskSummaryMapper;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.StatusSummary;

public class SubtaskRepositoryTest extends AbstractSubtaskRepositoryTest {
	
	@Autowired
	private SubtaskSummaryMapper mapper;
	
	@Test
	void findByTask() {
		getTasks().forEach(t -> {
			List<Subtask> subtasks = getSubtaskRepository().findAllByTaskId(
					t.getId(), 
					Pageable.ofSize(50), 
					null, 
					null
					)
					.collectList().block();
			
			assertTrue(subtasks.size() == getMaxSubtaskPerTask() || subtasks.size() == 50);
		});
	}
	
	@Test
	void findByTaskWithFilter() {
		getTasks().forEach(t -> {
			List<Subtask> subtasks = getSubtaskRepository().findAllByTaskId(
					t.getId(), 
					Pageable.ofSize(50), 
					"ID", 
					TaskStatusType.PENDING
					)
					.collectList().block();
			
			assertTrue(subtasks.size() == getMaxSubtaskPerTask() || subtasks.size() == 50);
		});
	}
	
	@Test
	void findPositionSummaryByTaskId() {
		getTasks().forEach(t -> {
			List<PositionSummary> idPositions = getSubtaskRepository().findPositionSummaryByTaskIdOrderByASC(t.getId())
					.collectList().block();

			getSubtasks().stream()
			.sorted(Comparator.comparing(Subtask::getPosition))
				.filter(s -> s.getTaskId() == t.getId())
				.forEach(s -> {
					PositionSummary idPosition = idPositions.stream()
							.filter(ip -> ip.getId() == s.getId())
							.findFirst().orElseThrow();
					
					assertTrue(idPosition.getId() == s.getId());
					assertTrue(idPosition.getPosition().compareTo(s.getPosition()) == 0);
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
		assertEquals(subtask.getPosition().compareTo(positionSummary.getPosition()), 0);
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
		
		subtasks.forEach(s -> {
			PositionSummary relatedValue = related.stream()
					.filter(r -> r.getId() == s.getId())
					.findFirst().get();
			
			assertEquals(s.getPosition().compareTo(relatedValue.getPosition()), 0);
		});
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
		
		sortedSubtasks.sort(Comparator.comparingDouble((s) -> s.getPosition().doubleValue()));
		
		int subtaskIndex = new Random().nextInt(0, sortedSubtasks.size());
		Subtask subtask = sortedSubtasks.get(subtaskIndex);
		final BigDecimal position = subtask.getPosition();
		
		Subtask lastSubtask = sortedSubtasks.get(subtaskIndex == sortedSubtasks.size()-1 ? 0 : sortedSubtasks.size()-1);
		final BigDecimal lastPosition = lastSubtask.getPosition();
		
		getSubtaskRepository().updatePriority(mapper.toPositionSummary(
				subtask.getId(),
				subtask.getVersion(),
				subtask.getTaskId(),
				subtask.getPosition()), lastPosition.compareTo(position) > 0 ? lastPosition.add(BigDecimal.ONE) : position.add(BigDecimal.ONE)
				).block();
		
		Subtask upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
		Subtask upLastSubtask = getSubtaskRepository().findById(lastSubtask.getId()).block();
		
		subtask.setPosition(upSubtask.getPosition());
		subtask.setVersion(upSubtask.getVersion());
		
		lastSubtask.setPosition(upLastSubtask.getPosition());
		lastSubtask.setVersion(upLastSubtask.getVersion());
		
		BigDecimal toAssertTrue = lastPosition.compareTo(position) > 0 ? lastPosition.add(BigDecimal.ONE) : position.add(BigDecimal.ONE);
		
		assertTrue(subtask.getPosition().compareTo(toAssertTrue) == 0);
		
		getSubtaskRepository().updatePriority(mapper.toPositionSummary(
				lastSubtask.getId(),
				lastSubtask.getVersion(),
				lastSubtask.getTaskId(),
				position), position).block();
		
		upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
		upLastSubtask = getSubtaskRepository().findById(lastSubtask.getId()).block();
		
		subtask.setPosition(upSubtask.getPosition());
		subtask.setVersion(upSubtask.getVersion());
		
		lastSubtask.setPosition(upLastSubtask.getPosition());
		lastSubtask.setVersion(upLastSubtask.getVersion());
		
		assertEquals(lastSubtask.getPosition().compareTo(position), 0);
		
		assertThrows(DataIntegrityViolationException.class, 
				() -> getSubtaskRepository().updatePriority(mapper.toPositionSummary(
						subtask.getId(), 
						subtask.getVersion(),
						subtask.getTaskId(),
						subtask.getPosition()), lastSubtask.getPosition()).block());
	}
	
	@Test
	void updatePriorityWithVersion() {
	    Task task = getTasks().get(0);
	    List<Subtask> subtasks = getSubtasks().stream()
	    		.filter(s -> s.getTaskId() == task.getId())
	    		.toList();
	    
	    Subtask subtask = subtasks.get(0);

	    long initialVersion = subtask.getVersion();
	    BigDecimal newPosition = subtask.getPosition()
	    		.add(BigDecimal.valueOf(subtasks.size()+1));
 
	    getSubtaskRepository().updatePriority(mapper.toPositionSummary(
	    		subtask.getId(),
	    		initialVersion,
	    		subtask.getTaskId(),
	    		subtask.getPosition()), newPosition).block();

	    Subtask upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
	    
	    subtask.setPosition(upSubtask.getPosition());
	    subtask.setVersion(upSubtask.getVersion());

	    assertEquals(initialVersion + 1, subtask.getVersion());
	    assertEquals(newPosition.compareTo(subtask.getPosition()), 0);
	    
	    assertThrows(OptimisticLockingFailureException.class, 
	    		() -> getSubtaskRepository().updatePriority(mapper.toPositionSummary(
	    						subtask.getId(),
	    	    				initialVersion,
	    	    				subtask.getTaskId(),
	    	    				subtask.getPosition()), newPosition.add(BigDecimal.ONE)).block()
		);
	    
	    getSubtaskRepository().updatePriority(mapper.toPositionSummary(
	    		subtask.getId(),
	    		subtask.getVersion(),
	    		subtask.getTaskId(),
	    		subtask.getPosition()), newPosition.add(BigDecimal.valueOf(2))).block();

	    upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
	    
	    subtask.setPosition(upSubtask.getPosition());
	    subtask.setVersion(upSubtask.getVersion());
	    
	    assertEquals(initialVersion+2, subtask.getVersion());
	    assertEquals(newPosition.add(BigDecimal.valueOf(2)).compareTo(subtask.getPosition()), 0);
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
				.map(s -> (BasicSummary) mapper.toStatusSummary(
						s.getId(),
						s.getVersion(),
						s.getTaskId(),
						s.getStatus()))
				.toList();
		
		getSubtaskRepository().updateStatus(mapper.toStatusSummary(
				subtask.getId(),
				subtask.getVersion(),
				subtask.getTaskId(),
				subtask.getStatus()
				), TaskStatusType.COMPLETED).block();
		
		Subtask upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
		
		subtask.setStatus(upSubtask.getStatus());
		subtask.setVersion(upSubtask.getVersion());
		
		ids = new ArrayList<>(ids);
		
		ids.set(index, mapper.toStatusSummary(
				subtask.getId(),
				subtask.getVersion(),
				subtask.getTaskId(),
				subtask.getStatus()));
		
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
	    
	    getSubtaskRepository().updateStatus(mapper.toStatusSummary(
	    		subtask.getId(),
	    		subtask.getVersion(),
	    		subtask.getTaskId(),
	    		subtask.getStatus()
	    		), TaskStatusType.COMPLETED).block();

	    Subtask upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
	    
	    subtask.setStatus(upSubtask.getStatus());
	    subtask.setVersion(upSubtask.getVersion());
	    
	    assertEquals(TaskStatusType.COMPLETED, subtask.getStatus());
	    
	    assertThrows(OptimisticLockingFailureException.class, 
	    		() -> getSubtaskRepository().updateStatus(mapper.toStatusSummary(
	    				subtask.getId(),
	    				subtask.getVersion() - 1,
	    				subtask.getTaskId(),
	    				subtask.getStatus()
	    				), TaskStatusType.COMPLETED).block()
	    		);
	    
	    upSubtask = getSubtaskRepository().findById(subtask.getId()).block();
	    
	    subtask.setStatus(upSubtask.getStatus());
	    subtask.setVersion(upSubtask.getVersion());
	    
	    assertEquals(TaskStatusType.COMPLETED, subtask.getStatus());
	    
	    List<BasicSummary> basics = getSubtasks().stream()
	            .map(s -> (BasicSummary) mapper.toStatusSummary(
	            		s.getId(),
	            		s.getVersion(),
	            		s.getTaskId(),
	            		s.getStatus()
	            		))
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

	@Test
	void sumVersionWithFilter() {
		getTasks().forEach(t -> {
			
			List<Subtask> subtasks = getSubtasks().stream()
					.filter(s -> t.getId().equals(s.getTaskId()))
					.toList();
			
			long sumByTask = subtasks.stream()
					.map(Subtask::getVersion)
					.reduce(Long::sum)
					.orElse(0L);
			
			assertEquals(
					sumByTask,
					getSubtaskRepository().sumVersionByTask(
							t.getId(), 
							Pageable.ofSize(getMaxSubtaskPerTask()),
							"ID", 
							TaskStatusType.PENDING).block());
		});
	}

}
