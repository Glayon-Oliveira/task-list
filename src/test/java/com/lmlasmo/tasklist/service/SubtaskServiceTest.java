package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class SubtaskServiceTest {

	@Mock
	private SubtaskRepository subtaskRepository;

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

		List<PositionSummary> idPositions = List.of(new PositionSummary(1, 1));

		when(subtaskRepository.findPositionSummaryByTaskId(taskId)).thenReturn(idPositions);
		when(subtaskRepository.save(any(Subtask.class))).thenReturn(subtask);

		assertDoesNotThrow(() -> subtaskService.save(create));
	}

	@Test
	void delete() {
		int maxId = 10;
		List<Integer> ids = new ArrayList<>();

		for(int cc = 1; cc <= maxId; cc++) {
			when(subtaskRepository.existsById(cc)).thenReturn(true);
			ids.add(cc);
		}

		when(subtaskRepository.existsById(maxId+1)).thenReturn(false);

		assertDoesNotThrow(() -> subtaskService.delete(ids));

		ids.add(maxId+1);

		assertThrows(EntityNotFoundException.class, () ->  subtaskService.delete(ids));
	}

	@Test
	void updatePosition() {
		List<PositionSummary> idPositions = new ArrayList<>();
		int maxId = 10;

		for(int cc = 1; cc <= maxId; cc++) {
			PositionSummary idPosition = new PositionSummary(cc, cc);
			idPositions.add(idPosition);
		}

		idPositions.forEach(ip -> {
			int randomPosition = new Random().nextInt(1, maxId+1);
			int newPosition =(randomPosition != ip.getPosition()) ? randomPosition : maxId/2;

			when(subtaskRepository.findPositionSummaryById(maxId+1)).thenReturn(Optional.empty());
			when(subtaskRepository.findPositionSummaryById(ip.getId())).thenReturn(Optional.of(ip));
			when(subtaskRepository.findPositionSummaryByRelatedSubtaskId(ip.getId())).thenReturn(new ArrayList<>(idPositions));

			assertThrows(EntityNotFoundException.class, () -> subtaskService.updatePosition(maxId+1, newPosition));
			assertThrows(EntityExistsException.class, () -> subtaskService.updatePosition(ip.getId(), ip.getPosition()));
			assertDoesNotThrow(() -> subtaskService.updatePosition(ip.getId(), newPosition));
		});

	}

}
