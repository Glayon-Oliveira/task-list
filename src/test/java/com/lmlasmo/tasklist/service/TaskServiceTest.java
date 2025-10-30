package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateTaskDTO;
import com.lmlasmo.tasklist.exception.EntityNotDeleteException;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.TaskRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@InjectMocks
	private TaskService taskService;

	@Test
	void create() {
		int userId = 1;

		CreateTaskDTO create = new CreateTaskDTO();
		create.setDeadline(OffsetDateTime.now());
		create.setDeadlineZone(ZoneId.systemDefault().toString());

		Task task = new Task(create, new User(userId));
		task.setId(1);

		when(taskRepository.save(any(Task.class))).thenReturn(task);

		assertDoesNotThrow(() -> taskService.save(create, userId));
	}

	@Test
	void delete() {
		int id = 1;
		int nId = 2;

		when(taskRepository.existsById(id)).thenReturn(true);
		when(taskRepository.existsById(nId)).thenReturn(false);

		assertThrows(EntityNotFoundException.class, () -> taskService.delete(nId));
		assertThrows(EntityNotDeleteException.class, () -> taskService.delete(id));
	}

	@Test
	void updateDescription() {
		int id = 1;
		int nId = 2;

		Task task = new Task();
		task.setId(id);
		task.setName(UUID.randomUUID().toString());
		task.setSummary(UUID.randomUUID().toString());
		task.setDeadline(Instant.now());
		task.setDeadlineZone(ZoneId.of("UTC").toString());

		UpdateTaskDTO update = new UpdateTaskDTO();
		update.setName(UUID.randomUUID().toString());
		update.setSummary(UUID.randomUUID().toString());

		when(taskRepository.findById(id)).thenReturn(Optional.of(task));
		when(taskRepository.findById(nId)).thenReturn(Optional.empty());
		when(taskRepository.save(task)).thenReturn(task);

		assertDoesNotThrow(() -> taskService.update(id, update));
		assertThrows(EntityNotFoundException.class, () -> taskService.update(nId, update));

		task.setName(update.getName());
		task.setSummary(update.getSummary());
		when(taskRepository.save(task)).thenReturn(task);

		assertEquals(update.getName(), taskService.update(id, update).getName());
		assertEquals(update.getSummary(), taskService.update(id, update).getSummary());
	}

	@Test
	void updateDeadline() {
		int id = 1;
		int nId = 2;

		Task task = new Task(id);
		task.setId(id);
		task.setName(UUID.randomUUID().toString());
		task.setSummary(UUID.randomUUID().toString());
		task.setDeadline(Instant.now());
		task.setDeadlineZone(ZoneId.of("UTC").toString());

		UpdateTaskDTO update = new UpdateTaskDTO();
		update.setDeadline(OffsetDateTime.now().plusHours(1));
		update.setDeadlineZone(ZoneId.getAvailableZoneIds().iterator().next());

		task.setDeadline(update.getDeadline().toInstant());
		task.setDeadlineZone(update.getDeadlineZone());

		when(taskRepository.findById(id)).thenReturn(Optional.of(task));
		when(taskRepository.save(any())).thenReturn(task);
		when(taskRepository.findById(nId)).thenReturn(Optional.empty());

		assertDoesNotThrow(() -> taskService.update(id, update));
		assertThrows(EntityNotFoundException.class, () -> taskService.update(nId, update));

		task.setDeadline(update.getDeadline().toInstant());
		task.setDeadlineZone(update.getDeadlineZone());
		when(taskRepository.save(task)).thenReturn(task);

		assertEquals(update.getDeadline().toInstant().atZone(ZoneId.of(update.getDeadlineZone())).toOffsetDateTime(), taskService.update(id, update).getDeadline());
		assertEquals(update.getDeadlineZone(), taskService.update(id, update).getDeadlineZone());
	}

}
