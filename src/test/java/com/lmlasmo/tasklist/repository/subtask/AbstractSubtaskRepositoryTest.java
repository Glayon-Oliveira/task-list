package com.lmlasmo.tasklist.repository.subtask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.task.AbstractTaskRepositoryTest;

import lombok.Getter;

public class AbstractSubtaskRepositoryTest extends AbstractTaskRepositoryTest {

	@Getter
	@Autowired
	private SubtaskRepository subtaskRepository;

	public static final int maxSubtaskPerTask = 5;

	@Getter
	private List<Subtask> subtasks = new ArrayList<>();

	@Override
	@BeforeEach
	public void setUp() {
		super.setUp();

		getTasks().forEach(t -> {
			String name = "Task - ID = " + UUID.randomUUID().toString();
			String summary = "Summary - ID = " + UUID.randomUUID().toString();

			for(int cc = 0; cc < maxSubtaskPerTask; cc++) {
				Subtask subtask = new Subtask();
				subtask.setName(name);
				subtask.setSummary(summary);
				subtask.setPosition(subtasks.size()+1);
				subtask.setTask(new Task(t.getId()));
				t.getSubtasks().add(subtask);

				subtask = subtaskRepository.save(subtask);
				subtasks.add(subtask);
			}
		});

		subtasks = Collections.unmodifiableList(subtasks);
	}

}
