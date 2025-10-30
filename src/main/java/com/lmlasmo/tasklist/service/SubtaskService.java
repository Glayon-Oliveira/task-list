package com.lmlasmo.tasklist.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.dto.SubtaskDTO;
import com.lmlasmo.tasklist.dto.create.CreateSubtaskDTO;
import com.lmlasmo.tasklist.dto.update.UpdateSubtaskDTO;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.service.applier.UpdateSubtaskApplier;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class SubtaskService {

	private SubtaskRepository subtaskRepository;
		
	public SubtaskDTO save(CreateSubtaskDTO create) {
		Subtask subtask = new Subtask(create);
		
		List<PositionSummary> idPositions = subtaskRepository.findPositionSummaryByTaskId(create.getTaskId());
		
		int maxPosition = idPositions.stream()
				.mapToInt(PositionSummary::getPosition)
				.max()
				.orElse(0);
		
		subtask.setPosition(maxPosition + 1);
				
		return new SubtaskDTO(subtaskRepository.save(subtask));
	}
	
	public void delete(List<Integer> subtaskIds) {
		long count = subtaskIds.stream()
				.filter(i -> subtaskRepository.existsById(i))
				.count();
		
		if(count < subtaskIds.size()) throw new EntityNotFoundException("Subtask not found");
		
		subtaskRepository.deleteAllByIdInBatch(subtaskIds);
	}
	
	public SubtaskDTO update(int subtaskId, UpdateSubtaskDTO update) {
		Subtask subtask = subtaskRepository.findById(subtaskId).orElseThrow(() -> new EntityNotFoundException("Subtask not found for id equals " + subtaskId));
		
		UpdateSubtaskApplier.apply(update, subtask);
		
		return new SubtaskDTO(subtaskRepository.save(subtask));
	}
	
	@Transactional
	public void updatePosition(int subtaskId, int position) {
		PositionSummary subtask = subtaskRepository.findPositionSummaryById(subtaskId)
				.orElseThrow(() -> new EntityNotFoundException("Subtask not found for id " + subtaskId));
		
		if(subtask.getPosition() == position) throw new EntityExistsException("New position of subtask is equals at exists position");
		
		List<PositionSummary> sublingSubtasks = subtaskRepository.findPositionSummaryByRelatedSubtaskId(subtaskId);
		
		subtaskRepository.updatePriority(subtask, 0);
		subtask = new PositionSummary(subtask.getId(), subtask.getVersion()+1, subtask.getPosition());
		
		normalizePositions(sublingSubtasks, subtask, position)
			.forEach(s -> subtaskRepository.updatePriority(s, s.getPosition()));
	}

	private List<PositionSummary> normalizePositions(List<PositionSummary> siblingSubtasks, PositionSummary subtaskToMove, int targetPosition){
		List<PositionSummary> resultList = new ArrayList<>();	
		int maxPosition = siblingSubtasks.size() + 1;
		final int finalPosition = Math.max(1, Math.min(targetPosition, maxPosition));		
		
		boolean isAscRasult = (finalPosition >= subtaskToMove.getPosition());
				
		subtaskToMove = new PositionSummary(subtaskToMove.getId(), subtaskToMove.getVersion(), finalPosition);
		
		siblingSubtasks.sort(Comparator.comparingInt(PositionSummary::getPosition));
		Iterator<PositionSummary> siblingIt = siblingSubtasks.iterator();
		
		for(int pos = 1; pos <= maxPosition; pos++) {
			
			if(finalPosition != pos) {								
				PositionSummary subtask = siblingIt.next();
				siblingIt.remove();
				
				if(subtask.getPosition() != pos) resultList.add(new PositionSummary(subtask.getId(), subtask.getVersion(), pos));
			}
		}
		
		Comparator<PositionSummary> comparator = Comparator.comparingInt(PositionSummary::getPosition);
		
		if(!isAscRasult) comparator = comparator.reversed();
		
		resultList.sort(comparator);
		resultList.add(subtaskToMove);
		
		return Collections.unmodifiableList(resultList);
	}
	
	public boolean existsByIdAndVersion(int id, long version) {
		return subtaskRepository.existsByIdAndVersion(id, version);
	}
	
	public long sumVersionByIds(Iterable<Integer> ids) {
		return subtaskRepository.sumVersionByids(ids);
	}
	
	public long sumVersionByTask(int taskId) {
		return subtaskRepository.sumVersionByTask(taskId);
	}
	
	public Page<SubtaskDTO> findByTask(int taskId, Pageable pageable){		
		return subtaskRepository.findByTaskId(taskId, pageable).map(SubtaskDTO::new);
	}

	public SubtaskDTO findById(int subtaskId) {
		return subtaskRepository.findById(subtaskId).map(SubtaskDTO::new)
				.orElseThrow(() -> new EntityNotFoundException("Subtask not found for id equals " + subtaskId));
	}	
	
}
