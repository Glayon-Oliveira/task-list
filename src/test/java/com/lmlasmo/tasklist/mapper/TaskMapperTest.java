package com.lmlasmo.tasklist.mapper;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.mapper.summary.TaskSummaryMapper;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;

@ExtendWith(SpringExtension.class)
@Import(MapperTestConfig.class)
public class TaskMapperTest {
	
	@Autowired
	private TaskMapper mapper;
	
	@Autowired
	private TaskSummaryMapper summaryMapper;

	private String name = "Name - ID = " + UUID.randomUUID().toString();
	private	String summary = "Summary - ID = " + UUID.randomUUID().toString();
	private OffsetDateTime deadline = OffsetDateTime.now();
	private String deadlineZone = ZoneId.systemDefault().toString();
	private Instant createdAt = Instant.now();
	private Instant updatedAt = createdAt;
	private User user = new User(1);

	@Test
	void createToTask() {
		CreateTaskDTO create = new CreateTaskDTO();
		create.setName(name);
		create.setSummary(summary);
		create.setDeadline(deadline);
		create.setDeadlineZone(deadlineZone);

		Task task = mapper.toEntity(create);
		task.setUserId(user.getId());

		assertEquals(task.getName(), create.getName());
		assertEquals(task.getSummary(), create.getSummary());
		assertEquals(task.getDeadline(), create.getDeadline().toInstant());
		assertEquals(task.getDeadlineZone(), create.getDeadlineZone());
	}

	@Test
	void taskToDTO() {
		Task task = new Task(1);
		task.setName(name);
		task.setSummary(summary);
		task.setDeadline(deadline.toInstant());
		task.setDeadlineZone(deadlineZone);
		task.setCreatedAt(createdAt);
		task.setUpdatedAt(updatedAt);
		task.setUserId(user.getId());

		TaskDTO dto = mapper.toDTO(task);

		assertEquals(task.getName(), dto.getName());
		assertEquals(task.getSummary(), dto.getSummary());
		assertEquals(task.getDeadline(), dto.getDeadline().toInstant());
		assertEquals(task.getDeadlineZone(), dto.getDeadlineZone());
		assertEquals(task.getCreatedAt(), dto.getCreatedAt());
		assertEquals(task.getUpdatedAt(), dto.getUpdatedAt());
	}
	
	@Test
	void entityToSummary() {
		Task task = new Task(1);
		task.setName(name);
		task.setSummary(summary);
		task.setDeadline(deadline.toInstant());
		task.setDeadlineZone(deadlineZone);
		task.setCreatedAt(createdAt);
		task.setUpdatedAt(updatedAt);
		task.setUserId(user.getId());
		
		Set<String> includedFields = TaskSummary.FIELDS.stream()
				.filter(st -> !st.equals("name"))
				.collect(Collectors.toSet());
		
		TaskSummary summary = summaryMapper.toSummary(task, includedFields);
		
		assertEquals(summary.getId().get(), task.getId());
		assertFalse(summary.getName().isPresent());
		assertEquals(summary.getSummary().get(), task.getSummary());
		assertEquals(summary.getDeadline().get(), task.getDeadline());
		assertEquals(summary.getDeadlineZone().get(), task.getDeadlineZone());
		assertEquals(summary.getCreatedAt().get(), task.getCreatedAt());
		assertEquals(summary.getUpdatedAt().get(), task.getUpdatedAt());
	}

}
