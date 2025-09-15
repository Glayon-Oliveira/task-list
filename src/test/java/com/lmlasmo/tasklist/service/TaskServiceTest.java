package com.lmlasmo.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lmlasmo.tasklist.dto.create.CreateTaskDTO;
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

		System.out.println(task.getId());

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

}
