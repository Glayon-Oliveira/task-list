package com.lmlasmo.tasklist.repository.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.StatusSummary;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class SubtaskRepositoryImpl implements SubtaskRepositoryCustom{

	private EntityManager entityManager;

	@Override
	@Transactional(readOnly = true)
	public List<PositionSummary> findPositionSummaryByTaskId(int taskId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<PositionSummary> criteriaQuery = criteriaBuilder.createQuery(PositionSummary.class);		
		Root<Subtask> root = criteriaQuery.from(Subtask.class);
		
		criteriaQuery.select(criteriaBuilder.construct(
				PositionSummary.class,
				root.get("id"),
				root.get("version"),
				root.get("position")
				))
		.where(criteriaBuilder.equal(root.get("task").get("id"), taskId));
		
		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PositionSummary> findPositionSummaryById(int subtaskId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<PositionSummary> criteriaQuery = criteriaBuilder.createQuery(PositionSummary.class);
		Root<Subtask> root = criteriaQuery.from(Subtask.class);
		
		criteriaQuery.select(criteriaBuilder.construct(
				PositionSummary.class,
				root.get("id"),
				root.get("version"),
				root.get("position")))
			.where(criteriaBuilder.equal(root.get("id"), subtaskId));
		
		try {
			return Optional.of(entityManager.createQuery(criteriaQuery).getSingleResult());
		}catch(NoResultException e) {
			return Optional.empty();
		}		
	}

	@Override
	@Transactional(readOnly = true)
	public List<PositionSummary> findPositionSummaryByRelatedSubtaskId(int subtaskId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<PositionSummary> criteriaQuery = criteriaBuilder.createQuery(PositionSummary.class);
		Root<Subtask> root = criteriaQuery.from(Subtask.class);
		
		Subquery<Task> subquery = criteriaQuery.subquery(Task.class);
		Root<Subtask> subRoot = subquery.from(Subtask.class);
		
		subquery.select(subRoot.get("task"))
			.where(criteriaBuilder.equal(subRoot.get("id"), subtaskId));
		
		criteriaQuery.select(criteriaBuilder.construct(
				PositionSummary.class,
				root.get("id"),
				root.get("version"),
				root.get("position")
				))
		.where(criteriaBuilder.and(
				root.get("task").in(subquery),
				criteriaBuilder.notEqual(root.get("id"), subtaskId)
				));
		
		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	@Transactional
	public void updatePriority(BasicSummary basic, int position) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaUpdate<Subtask> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Subtask.class);
		Root<Subtask> root = criteriaUpdate.from(Subtask.class);
		
		criteriaUpdate.set(root.get("position"), position)
			.set(root.get("version"), basic.getVersion()+1)
			.where(criteriaBuilder.and(
					criteriaBuilder.equal(root.get("id"), basic.getId()),
					criteriaBuilder.equal(root.get("version"), basic.getVersion())
					));
		
		int rows = entityManager.createQuery(criteriaUpdate).executeUpdate();
		
		if(rows == 0) throw new OptimisticLockException("Row was updated or deleted by another transaction");
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StatusSummary> findStatusSummaryById(int subtaskId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<StatusSummary> criteriaQuery = criteriaBuilder.createQuery(StatusSummary.class);
		Root<Subtask> root = criteriaQuery.from(Subtask.class);
		
		criteriaQuery.select(criteriaBuilder.construct(
				StatusSummary.class,
				root.get("id"),
				root.get("version"),
				root.get("status"),
				root.get("task").get("id")
				))
		.where(criteriaBuilder.equal(root.get("id"), subtaskId));
		
		try {
			return Optional.of(entityManager.createQuery(criteriaQuery).getSingleResult());
		}catch(NoResultException e) {
			return Optional.empty();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<StatusSummary> findStatusSummaryByIds(Iterable<Integer> subtaskIds) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<StatusSummary> criteriaQuery = criteriaBuilder.createQuery(StatusSummary.class);
		Root<Subtask> root = criteriaQuery.from(Subtask.class);
		
		List<Integer> ids = new ArrayList<>();
		subtaskIds.forEach(ids::add);
		
		criteriaQuery.select(criteriaBuilder.construct(
				StatusSummary.class,
				root.get("id"),
				root.get("version"),
				root.get("status"),
				root.get("task").get("id")
				))
		.where(root.get("id").in(ids));
		
		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	@Transactional
	public void updateStatus(BasicSummary basic, TaskStatusType status) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaUpdate<Subtask> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Subtask.class);
		Root<Subtask> root = criteriaUpdate.from(Subtask.class);
		
		criteriaUpdate.set(root.get("status"), status)
			.set(root.get("version"), basic.getVersion()+1)
			.where(criteriaBuilder.and(
					criteriaBuilder.equal(root.get("id"), basic.getId()),
					criteriaBuilder.equal(root.get("version"), basic.getVersion())
					));
		
		int rows = entityManager.createQuery(criteriaUpdate).executeUpdate();
		
		if(rows == 0) throw new OptimisticLockException("Row was updated or deleted by another transaction");
	}

	@Override
	@Transactional
	public void updateStatus(Iterable<? extends BasicSummary> basics, TaskStatusType status) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		for(BasicSummary basic: basics) {
			CriteriaUpdate<Subtask> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Subtask.class);
			Root<Subtask> root = criteriaUpdate.from(Subtask.class);
			
			criteriaUpdate.set(root.get("status"), status)
				.set(root.get("version"), basic.getVersion()+1)
				.where(criteriaBuilder.and(
						criteriaBuilder.equal(root.get("id"), basic.getId()),
						criteriaBuilder.equal(root.get("version"), basic.getVersion())
						));
			
			int rows = entityManager.createQuery(criteriaUpdate).executeUpdate();
			
			if(rows == 0) throw new OptimisticLockException("Row with id " + basic.getId() + " was updated or deleted by another transaction");
		}
	}
	
	@Override
	public long sumVersionByids(Iterable<Integer> ids) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<Task> root = criteriaQuery.from(Task.class);
		
		criteriaQuery.select(criteriaBuilder.sum(root.get("version")))
			.where(root.get("id").in(ids));
		
		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}

	@Override
	public long sumVersionByTask(int taskId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<Task> root = criteriaQuery.from(Task.class);
		
		criteriaQuery.select(criteriaBuilder.sum(root.get("version")))
			.where(criteriaBuilder.equal(root.get("task").get("id"), taskId));
		
		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}

}
