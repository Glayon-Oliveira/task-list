package com.lmlasmo.tasklist.mapper;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.mapper.summary.SubtaskSummaryMapper;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

@ExtendWith(SpringExtension.class)
@Import(MapperTestConfig.class)
public class SubtaskMapperTest {
	
	@Autowired
	private SubtaskMapper mapper;
	
	@Autowired
	private SubtaskSummaryMapper summaryMapper;

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

		assertEquals(subtask.getName(), create.getName());
		assertEquals(subtask.getSummary(), create.getSummary());
		assertEquals(subtask.getDurationMinutes(), create.getDurationMinutes());
		assertEquals(subtask.getTaskId(), create.getTaskId());
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

		assertEquals(dto.getId(), subtask.getId());
		assertEquals(dto.getName(), subtask.getName());
		assertEquals(dto.getSummary(), subtask.getSummary());
		assertEquals(dto.getDurationMinutes(), subtask.getDurationMinutes());
		assertEquals(dto.getPosition(), subtask.getPosition());
		assertEquals(dto.getCreatedAt(), subtask.getCreatedAt());
		assertEquals(dto.getUpdatedAt(), subtask.getUpdatedAt());
	}
	
	@Test
	void entityToSummary() {
		Subtask subtask = new Subtask();
		subtask.setId(1);
		subtask.setName(name);
		subtask.setSummary(summary);
		subtask.setDurationMinutes(durationMinutes);
		subtask.setPosition(position);
		subtask.setCreatedAt(createdAt);
		subtask.setUpdatedAt(updatedAt);
		subtask.setTaskId(task.getId());
		
		Set<String> includedFields = SubtaskSummary.FIELDS.stream()
				.filter(st -> !st.equals("name"))
				.collect(Collectors.toSet());
		
		SubtaskSummary summary = summaryMapper.toSummary(subtask, includedFields); 
				
		assertEquals(summary.getId().get(), subtask.getId());
		assertFalse(summary.getName().isPresent());
		assertEquals(summary.getSummary().get(), subtask.getSummary());
		assertEquals(summary.getDurationMinutes().get(), subtask.getDurationMinutes());
		assertEquals(summary.getPosition().get(), subtask.getPosition());
		assertEquals(summary.getCreatedAt().get(), subtask.getCreatedAt());
		assertEquals(summary.getUpdatedAt().get(), subtask.getUpdatedAt());
	}

}
