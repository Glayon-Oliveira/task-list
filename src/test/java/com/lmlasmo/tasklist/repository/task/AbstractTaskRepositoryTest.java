package com.lmlasmo.tasklist.repository.task;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.repository.TaskRepository;
import com.lmlasmo.tasklist.repository.user.AbstractUserRepositoryTest;

import lombok.Getter;

public class AbstractTaskRepositoryTest extends AbstractUserRepositoryTest {

	@Getter
	@Autowired
	private TaskRepository taskRepository;

	@Getter
	private final int maxTasksPerUser = 5;

	@Getter
	private List<Task> tasks = new ArrayList<>();

	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();

		getUsers().forEach(u -> {
			String name = "Task - ID = " + UUID.randomUUID().toString();
			String summary = "Summary - ID = " + UUID.randomUUID().toString();
			OffsetDateTime deadline = OffsetDateTime.now().withSecond(0).withNano(0);
			String deadlineZone = ZoneId.systemDefault().toString();

			for(int cc = 0; cc < 5; cc++) {
				Task task = new Task();
				task.setName(name);
				task.setSummary(summary);
				task.setDeadline(deadline.toInstant());
				task.setDeadlineZone(deadlineZone);
				task.setUser(new User(u.getId()));

				task = taskRepository.save(task);
				tasks.add(task);
			}
		});

		tasks = Collections.unmodifiableList(tasks);
	}

}
