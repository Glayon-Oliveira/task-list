package com.lmlasmo.tasklist.repository.subtask;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.IdPosition;

public class SubtaskRepositoryTest extends AbstractSubtaskRepositoryTest {

	@Test
	void findIdAndPositionByTaskId() {
		getTasks().forEach(t -> {
			List<IdPosition> idPositions = getSubtaskRepository().findIdAndPositionByTaskId(t.getId());

			t.getSubtasks().forEach(s -> {
				IdPosition idPosition = idPositions.stream()
						.filter(ip -> ip.getId() == s.getId())
						.findFirst().orElseThrow();

				assertTrue(idPosition.getId() == s.getId());
				assertTrue(idPosition.getPosition() == s.getPosition());
			});
		});
	}

	@Test
	void delete() {
		List<Integer> ids = getSubtasks().stream()
				.map(Subtask::getId)
				.toList();

		getSubtaskRepository().deleteAllByIdInBatch(ids);
	}

}
