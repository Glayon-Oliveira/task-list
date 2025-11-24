package com.lmlasmo.tasklist.repository.subtask;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.lmlasmo.tasklist.dto.update.UpdateSubtaskPositionDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskPositionDTO.MovePositionType;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.service.SubtaskService;

@Import(SubtaskService.class)
public class SubtaskRepositoryWithServiceTest extends AbstractSubtaskRepositoryTest{

	@Autowired
	private SubtaskService subtaskService;
	
	@Test
	void updatePositionForAfter() {
		getTasks().forEach(t -> {
			List<PositionSummary> subtasks = getSubtaskRepository().findPositionSummaryByTaskIdOrderByASC(t.getId())
					.collectList()
					.block();
			
			assertTrue(subtasks.size() >= 5);
			
			PositionSummary subtask = subtasks.get(maxSubtaskPerTask-1);
			PositionSummary anchor = subtasks.get(0);
			PositionSummary second = subtasks.get(1);
			
			UpdateSubtaskPositionDTO update = new UpdateSubtaskPositionDTO();
			update.setMoveType(MovePositionType.AFTER);
			update.setAnchorSubtaskId(anchor.getId());
			
			subtaskService.updatePosition(subtask.getId(), update).block();
			subtask = getSubtaskRepository().findPositionSummaryById(subtask.getId()).block();
			
			assertTrue(subtask.getPosition().compareTo(second.getPosition()) < 0);
			
			subtask = subtasks.get(0);
			anchor = subtasks.get(maxSubtaskPerTask-2);
			update.setAnchorSubtaskId(anchor.getId());
			
			subtaskService.updatePosition(subtask.getId(), update).block();
			subtask = getSubtaskRepository().findPositionSummaryById(subtask.getId()).block();
			
			assertTrue(subtask.getPosition().compareTo(anchor.getPosition()) > 0);
		});
	}
	
	@Test
	void updatePositionForBefore() {
		getTasks().forEach(t -> {
			List<PositionSummary> subtasks = getSubtaskRepository().findPositionSummaryByTaskIdOrderByASC(t.getId())
					.collectList()
					.block();
			
			assertTrue(subtasks.size() >= 5);
			
			PositionSummary subtask = subtasks.get(0);
			PositionSummary anchor = subtasks.get(maxSubtaskPerTask-1);
			PositionSummary second = subtasks.get(maxSubtaskPerTask-2);
			
			UpdateSubtaskPositionDTO update = new UpdateSubtaskPositionDTO();
			update.setMoveType(MovePositionType.BEFORE);
			update.setAnchorSubtaskId(anchor.getId());
			
			subtaskService.updatePosition(subtask.getId(), update).block();
			subtask = getSubtaskRepository().findPositionSummaryById(subtask.getId()).block();
			
			assertTrue(subtask.getPosition().compareTo(second.getPosition()) > 0);
			
			subtask = subtasks.get(maxSubtaskPerTask-1);
			anchor = subtasks.get(1);
			update.setAnchorSubtaskId(anchor.getId());
			
			subtaskService.updatePosition(subtask.getId(), update).block();
			subtask = getSubtaskRepository().findPositionSummaryById(subtask.getId()).block();
			
			assertTrue(subtask.getPosition().compareTo(anchor.getPosition()) < 0);
		});
	}
	
	@Test
	void updateWithNormalize() {
		getTasks().forEach(t -> {
			List<PositionSummary> subtasks = getSubtaskRepository().findPositionSummaryByTaskIdOrderByASC(t.getId())
					.collectList()
					.block();
			
			assertTrue(subtasks.size() >= 5);
			
			PositionSummary anchor = subtasks.get(0);
			PositionSummary second = subtasks.get(1);
			PositionSummary subtask = subtasks.get(2);
			
			getSubtaskRepository().updatePriority(anchor, new BigDecimal("0.0000000001")).block();
			getSubtaskRepository().updatePriority(second, new BigDecimal("0.0000000002")).block();
			
			anchor = getSubtaskRepository().findPositionSummaryById(anchor.getId()).block();
			second = getSubtaskRepository().findPositionSummaryById(second.getId()).block();
			
			UpdateSubtaskPositionDTO update = new UpdateSubtaskPositionDTO(MovePositionType.AFTER, anchor.getId());
			
			subtaskService.updatePosition(subtask.getId(), update).block();
			
			anchor = getSubtaskRepository().findPositionSummaryById(anchor.getId()).block();
			second = getSubtaskRepository().findPositionSummaryById(second.getId()).block();
			
			assertTrue(anchor.getPosition().compareTo(BigDecimal.ONE) >= 0);
			assertTrue(anchor.getPosition().compareTo(BigDecimal.ONE) >= 0);
		});
	}

	@RepeatedTest(2)
	void updatePosition(RepetitionInfo info) {
		getTasks().forEach(t -> {
			
			List<Subtask> subtasks = getSubtasks().stream()
					.filter(s -> s.getTaskId() == t.getId())
					.toList();
			
			Subtask subtask = subtasks.get(2);
			subtask = getSubtaskRepository().findById(subtask.getId()).block();
			
			Subtask firstAnchor = subtasks.get(0);
			firstAnchor = getSubtaskRepository().findById(firstAnchor.getId()).block();
			
			Subtask lastAnchor = subtasks.get(subtasks.size()-1);
			lastAnchor = getSubtaskRepository().findById(lastAnchor.getId()).block();
			
			UpdateSubtaskPositionDTO update = new UpdateSubtaskPositionDTO();
			
			if(info.getCurrentRepetition() == 1) {
				update.setMoveType(MovePositionType.AFTER);
				update.setAnchorSubtaskId(firstAnchor.getId());
			}else {
				update.setMoveType(MovePositionType.BEFORE);
				update.setAnchorSubtaskId(lastAnchor.getId());
			}
			
			subtaskService.updatePosition(subtask.getId(), update).block();
			
			subtask = getSubtaskRepository().findById(subtask.getId()).block();
			
			if(info.getCurrentRepetition() == 1) {
				assertTrue(subtask.getPosition().compareTo(subtasks.get(1).getPosition()) < 0);
			}else {
				assertTrue(subtask.getPosition().compareTo(subtasks.get(3).getPosition()) > 0);
			}
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
