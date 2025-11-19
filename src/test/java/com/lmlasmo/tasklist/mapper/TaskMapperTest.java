package com.lmlasmo.tasklist.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lmlasmo.tasklist.dto.TaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.User;

public class TaskMapperTest {

	private String name = "Name - ID = " + UUID.randomUUID().toString();
	private	String summary = "Summary - ID = " + UUID.randomUUID().toString();
	private OffsetDateTime deadline = OffsetDateTime.now();
	private String deadlineZone = ZoneId.systemDefault().toString();
	private Instant createdAt = Instant.now();
	private Instant updateAt = createdAt;
	private User user = new User(1);

	@Test
	void createToTask() {
		CreateTaskDTO create = new CreateTaskDTO();
		create.setName(name);
		create.setSummary(summary);
		create.setDeadline(deadline);
		create.setDeadlineZone(deadlineZone);

		Task task = new Task(create, user.getId());

		assertTrue(task.getName().equals(create.getName()));
		assertTrue(task.getSummary().equals(create.getSummary()));
		assertTrue(task.getDeadline().equals(create.getDeadline().toInstant()));
		assertTrue(task.getDeadlineZone().equals(create.getDeadlineZone()));
	}

	@Test
	void taskToDTO() {
		Task task = new Task(1);
		task.setName(name);
		task.setSummary(summary);
		task.setDeadline(deadline.toInstant());
		task.setDeadlineZone(deadlineZone);
		task.setCreatedAt(createdAt);
		task.setUpdatedAt(updateAt);
		task.setUserId(user.getId());

		TaskDTO dto = new TaskDTO(task);

		assertTrue(task.getName().equals(dto.getName()));
		assertTrue(task.getSummary().equals(dto.getSummary()));
		assertTrue(task.getDeadline().equals(dto.getDeadline().toInstant()));
		assertTrue(task.getDeadlineZone().equals(dto.getDeadlineZone()));
		assertTrue(task.getCreatedAt().equals(dto.getCreatedAt()));
		assertTrue(task.getUpdatedAt().equals(dto.getUpdatedAt()));
	}

}
