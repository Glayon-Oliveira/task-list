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
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.repository.SubtaskRepository;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.IdPosition;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class SubtaskService {

	private SubtaskRepository subtaskRepository;
		
	public SubtaskDTO save(CreateSubtaskDTO create) {
		Subtask subtask = new Subtask(create);
		
		List<IdPosition> idPositions = subtaskRepository.findIdAndPositionByTaskId(create.getTaskId());
		
		int maxPosition = idPositions.stream()
				.mapToInt(IdPosition::getPosition)
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
	
	@Transactional
	public void updatePosition(int subtaskId, int position) {
		IdPosition subtask = subtaskRepository.findIdAndPositionById(subtaskId)
				.orElseThrow(() -> new EntityNotFoundException("Subtask not found for id " + subtaskId));
		
		if(subtask.getPosition() == position) throw new EntityExistsException("New position of subtask is equals at exists position");
		
		List<IdPosition> sublingSubtasks = subtaskRepository.findIdAndPositionByRelatedSubtaskId(subtaskId);
		
		subtaskRepository.updatePriority(subtask.getId(), 0);
		
		normalizePositions(sublingSubtasks, subtask, position).stream()
			.forEach(s -> subtaskRepository.updatePriority(s.getId(), s.getPosition()));
	}

	private List<IdPosition> normalizePositions(List<IdPosition> siblingSubtasks, IdPosition subtaskToMove, int targetPosition){
		List<IdPosition> resultList = new ArrayList<>();	
		int maxPosition = siblingSubtasks.size() + 1;
		final int finalPosition = Math.max(1, Math.min(targetPosition, maxPosition));		
		
		boolean isAscRasult = (finalPosition >= subtaskToMove.getPosition());
		
		final int subtaskIdToMove = subtaskToMove.getId();		
		subtaskToMove = new IdPosition() {
			public int getId() {return subtaskIdToMove;}
			public int getPosition() {return finalPosition;}
		};
		
		siblingSubtasks.sort(Comparator.comparingInt(IdPosition::getPosition));
		Iterator<IdPosition> siblingIt = siblingSubtasks.iterator();
		
		for(int pos = 1; pos <= maxPosition; pos++) {
			
			if(finalPosition != pos) {								
				IdPosition subtask = siblingIt.next();
				siblingIt.remove();
				
				if(subtask.getPosition() != pos) {
					final int position = pos;
					
					resultList.add(new IdPosition() {
						public int getId() {return subtask.getId();}
						public int getPosition() {return position;}
					}); 
				}
			}
		}
		
		Comparator<IdPosition> comparator = Comparator.comparingInt(IdPosition::getPosition);
		
		if(!isAscRasult) comparator = comparator.reversed();
		
		resultList.sort(comparator);
		resultList.add(subtaskToMove);
		
		return Collections.unmodifiableList(resultList);
	}	
	
	public Page<SubtaskDTO> findByTask(int taskId, Pageable pageable){		
		return subtaskRepository.findByTaskId(taskId, pageable).map(SubtaskDTO::new);
	}	
	
}
