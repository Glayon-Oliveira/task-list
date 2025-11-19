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

@Import(SubtaskService.class)
public class SubtaskRepositoryWithServiceTest extends AbstractSubtaskRepositoryTest{

	@Autowired
	private SubtaskService subtaskService;

	@RepeatedTest(maxSubtaskPerTask)
	void updatePosition(RepetitionInfo info) {		
		getSubtasks().forEach(s -> {

			int newPosition = new Random().nextInt(1, maxSubtaskPerTask+1);
			
			Subtask subtask = getSubtaskRepository().findById(s.getId()).block();
			
			s.setPosition(subtask.getPosition());
			s.setVersion(subtask.getVersion());

			if(newPosition == s.getPosition()) {
				newPosition = (s.getPosition() == maxSubtaskPerTask) ? newPosition-1 : maxSubtaskPerTask;
			}			

			final int originalPosition = s.getPosition();

			subtaskService.updatePosition(s.getId(), newPosition).block();
			
			subtask = getSubtaskRepository().findById(s.getId()).block();
			
			s.setPosition(subtask.getPosition());
			s.setVersion(subtask.getVersion());

			assertNotNull(subtask);
			assertTrue(subtask.getPosition() != originalPosition);
		});
	}

	@Test
	void delete() {
		List<Integer> ids = getSubtasks().stream()
				.map(Subtask::getId)
				.toList();

		subtaskService.delete(ids).block();
	}

}
