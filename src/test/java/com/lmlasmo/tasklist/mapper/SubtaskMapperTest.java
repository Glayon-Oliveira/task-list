package com.lmlasmo.tasklist.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

@ExtendWith(SpringExtension.class)
@Import(MapperTestConfig.class)
public class SubtaskMapperTest {
	
	@Autowired
	private SubtaskMapper mapper;

	private String name = "Name - ID = " + UUID.randomUUID().toString();
	private String summary = "Summary - ID = " + UUID.randomUUID().toString();
	private int durationMinutes = 10;
	private BigDecimal position = BigDecimal.ONE;
	private Instant createdAt = Instant.now();
	private Instant updatedAt = createdAt;
	private Task task = new Task(1);

	@Test
	void createToSubtask() {
		CreateSubtaskDTO create = new CreateSubtaskDTO();
		create.setName(name);
		create.setSummary(summary);
		create.setDurationMinutes(durationMinutes);
		create.setTaskId(task.getId());

		Subtask subtask = mapper.toEntity(create);

		assertTrue(subtask.getName().equals(create.getName()));
		assertTrue(subtask.getSummary().equals(create.getSummary()));
		assertTrue(subtask.getDurationMinutes() == create.getDurationMinutes());
		assertTrue(subtask.getTaskId() == create.getTaskId());
	}

	@Test
	void subtaskToDTO() {
		Subtask subtask = new Subtask();
		subtask.setId(1);
		subtask.setName(name);
		subtask.setSummary(summary);
		subtask.setDurationMinutes(durationMinutes);
		subtask.setPosition(position);
		subtask.setCreatedAt(createdAt);
		subtask.setUpdatedAt(updatedAt);
		subtask.setTaskId(task.getId());

		SubtaskDTO dto = mapper.toDTO(subtask);

		assertTrue(dto.getId() == subtask.getId());
		assertTrue(dto.getName().equals(subtask.getName()));
		assertTrue(dto.getSummary().equals(subtask.getSummary()));
		assertTrue(dto.getDurationMinutes() == subtask.getDurationMinutes());
		assertTrue(dto.getPosition() == subtask.getPosition());
		assertTrue(dto.getCreatedAt().equals(subtask.getCreatedAt()));
		assertTrue(dto.getUpdatedAt().equals(subtask.getUpdatedAt()));
	}
	
	@Test
	void subtaskSummaryToDTO() {
		SubtaskSummary subtask = new SubtaskSummary(
				1, name, summary, TaskStatusType.COMPLETED, position, durationMinutes, 1L, 
				createdAt, updatedAt, task.getId()
				);

		SubtaskDTO dto = mapper.toDTO(subtask);

		assertTrue(dto.getId() == subtask.getId());
		assertTrue(dto.getName().equals(subtask.getName()));
		assertTrue(dto.getSummary().equals(subtask.getSummary()));
		assertTrue(dto.getDurationMinutes() == subtask.getDurationMinutes());
		assertTrue(dto.getPosition() == subtask.getPosition());
		assertTrue(dto.getCreatedAt().equals(subtask.getCreatedAt()));
		assertTrue(dto.getUpdatedAt().equals(subtask.getUpdatedAt()));
	}

}
