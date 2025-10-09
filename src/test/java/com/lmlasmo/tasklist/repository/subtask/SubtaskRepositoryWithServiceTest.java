package com.lmlasmo.tasklist.repository.subtask;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.service.SubtaskService;

import jakarta.persistence.EntityManager;

@Import(SubtaskService.class)
public class SubtaskRepositoryWithServiceTest extends AbstractSubtaskRepositoryTest{

	@Autowired
	private SubtaskService subtaskService;

	@Autowired
	private EntityManager em;

	@RepeatedTest(maxSubtaskPerTask)
	void updatePosition(RepetitionInfo info) {
		getSubtasks().forEach(s -> {
			em.refresh(s);			

			int newPosition = new Random().nextInt(1, maxSubtaskPerTask+1);

			if(newPosition == s.getPosition()) {
				newPosition = (s.getPosition() == maxSubtaskPerTask) ? newPosition-1 : maxSubtaskPerTask;
			}			

			final int originalPosition = s.getPosition();

			subtaskService.updatePosition(s.getId(), newPosition);

			Subtask subtask = getSubtaskRepository().findById(s.getId()).orElse(null);
			em.refresh(subtask);

			assertNotNull(subtask);
			assertTrue(subtask.getPosition() != originalPosition);
		});
	}

	@Test
	void delete() {
		List<Integer> ids = getSubtasks().stream()
				.map(Subtask::getId)
				.toList();

		subtaskService.delete(ids);
	}

}
