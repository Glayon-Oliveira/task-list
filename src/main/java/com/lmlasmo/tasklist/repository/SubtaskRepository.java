package com.lmlasmo.tasklist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.IdPosition;

public interface SubtaskRepository extends JpaRepository<Subtask, Integer>{	
	
	@Query("SELECT s.id AS id, s.position AS position FROM Subtask s WHERE s.task.id = :taskId")
	public List<SubtaskSummary.IdPosition> findIdAndPositionByTaskId(int taskId);
	
	@Query("SELECT s.id AS id, s.position AS position FROM Subtask s WHERE s.id = :subtaskId")
	public Optional<SubtaskSummary.IdPosition> findIdAndPositionById(int subtaskId);
	
	@Query("""
			SELECT s.id AS id, s.position AS position
			FROM Subtask s
			WHERE s.task.id = (
			    SELECT parent.task.id
			    FROM Subtask parent
			    WHERE parent.id = :subtaskId
			)
			AND s.id <> :subtaskId
			""")
	public List<IdPosition> findIdAndPositionByRelatedSubtaskId(int subtaskId);
	
	@Query("SELECT s.id AS id, s.status AS status, s.task.id AS taskId FROM Subtask s WHERE s.id = :subtaskId")
	public Optional<SubtaskSummary.IdStatusTask> findIdAndStatusAndTaskById(int subtaskId);
	
	@Query("SELECT s.id AS id, s.status AS status, s.task.id AS taskId FROM Subtask s WHERE s.id IN :subtaskIds")
	public List<SubtaskSummary.IdStatusTask> findAllIdAndStatusAndTaskById(List<Integer> subtaskIds);

	public Page<Subtask> findByTaskId(int taskId, Pageable pageable);
	
	public boolean existsByTaskIdAndStatus(int taskId, TaskStatusType status);
	
	@Modifying
	@Transactional
	@Query("UPDATE Subtask s SET s.position = :position WHERE s.id = :subtaskId")
	public void updatePriority(int subtaskId, int position);
	
	@Modifying
	@Transactional
	@Query("UPDATE Subtask s SET s.status = :status WHERE s.id = :subtaskId")
	public void updateStatus(int subtaskId, TaskStatusType status);
		
	public long countByTaskId(int taskId);
	
}
