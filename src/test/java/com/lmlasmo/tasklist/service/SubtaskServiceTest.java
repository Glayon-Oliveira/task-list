package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.SubtaskRepository;

@ExtendWith(MockitoExtension.class)
public class SubtaskServiceTest {

	@Mock
	private SubtaskRepository subtaskRepository;

	@InjectMocks
	private SubtaskService subtaskService;

	@Test
	void create() {
		CreateSubtaskDTO create = new CreateSubtaskDTO();
		create.setName(UUID.randomUUID().toString());
		create.setSummary(UUID.randomUUID().toString());
		create.setDurationMinutes(5);
		create.setTaskId(1);

		Subtask subtask = new Subtask(create);

		when(subtaskRepository.findIdAndPositionByTaskId(anyInt())).thenReturn(List.of());
		when(subtaskRepository.save(any(Subtask.class))).thenReturn(subtask);

		assertDoesNotThrow(() -> subtaskService.save(create));
	}


}
