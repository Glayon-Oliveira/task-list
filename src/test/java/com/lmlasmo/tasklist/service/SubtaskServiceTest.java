package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.exception.ResourceAlreadyExistsException;
import com.lmlasmo.tasklist.exception.ResourceNotFoundException;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class SubtaskServiceTest {

	@Mock
	private SubtaskRepository subtaskRepository;
	
	@Mock
	private TransactionalOperator operator;

	@InjectMocks
	private SubtaskService subtaskService;

	@Test
	void create() {
		String name = "Name - ID = " + UUID.randomUUID().toString();
		int taskId = 1;

		CreateSubtaskDTO create = new CreateSubtaskDTO();
		create.setName(name);
		create.setTaskId(taskId);

		Subtask subtask = new Subtask(create);

		List<PositionSummary> idPositions = List.of(new PositionSummary(1, 0, 1));

		when(subtaskRepository.findPositionSummaryByTaskId(taskId)).thenReturn(Flux.fromIterable(idPositions));
		when(subtaskRepository.save(any(Subtask.class))).thenReturn(Mono.just(subtask));

		assertDoesNotThrow(() -> subtaskService.save(create).block());
	}

	@SuppressWarnings("unchecked")
	@Test
	void updatePosition() {
		List<PositionSummary> idPositions = new ArrayList<>();
		int maxId = 10;

		for(int cc = 1; cc <= maxId; cc++) {
			PositionSummary idPosition = new PositionSummary(cc, 0, cc);
			idPositions.add(idPosition);
		}

		idPositions.forEach(ip -> {
			int randomPosition = new Random().nextInt(1, maxId+1);
			int newPosition =(randomPosition != ip.getPosition()) ? randomPosition : maxId/2;

			when(subtaskRepository.updatePriority(any(), anyInt())).thenReturn(Mono.empty());
			when(subtaskRepository.findPositionSummaryById(maxId+1)).thenReturn(Mono.empty());
			when(subtaskRepository.findPositionSummaryById(ip.getId())).thenReturn(Mono.just(ip));
			when(subtaskRepository.findPositionSummaryByRelatedSubtaskId(ip.getId())).thenReturn(Flux.fromIterable(new ArrayList<>(idPositions)));
			
			when(subtaskRepository.getOperator()).thenReturn(operator);
			when(operator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

			assertThrows(ResourceNotFoundException.class, () -> subtaskService.updatePosition(maxId+1, newPosition).block());			
			assertThrows(ResourceAlreadyExistsException.class, () -> subtaskService.updatePosition(ip.getId(), ip.getPosition()).block());
			assertDoesNotThrow(() -> subtaskService.updatePosition(ip.getId(), newPosition).block());
		});

	}

}
