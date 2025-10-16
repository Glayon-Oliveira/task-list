package com.lmlasmo.tasklist.repository.custom;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TaskRepositoryImpl implements TaskRepositoryCustom{

	private EntityManager entityManager;
	
	@Override
	public Optional<StatusSummary> findStatusSummaryById(int taskId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<StatusSummary> criteriaQuery = criteriaBuilder.createQuery(StatusSummary.class);
		Root<Task> root = criteriaQuery.from(Task.class);
		
		criteriaQuery.select(criteriaBuilder.construct(
				StatusSummary.class,
				root.get("id"),
				root.get("version"),
				root.get("status")
				))
		.where(criteriaBuilder.equal(root.get("id"), taskId));
		
		try {
			return Optional.of(entityManager.createQuery(criteriaQuery).getSingleResult());
		}catch(NoResultException e) {
			return Optional.empty();
		}		
	}

	@Override
	@Transactional
	public void updateStatus(BasicSummary basic, TaskStatusType status) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaUpdate<Task> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Task.class);
		Root<Task> root = criteriaUpdate.from(Task.class);
		
		criteriaUpdate.set(root.get("status"), status)
			.set(root.get("version"), basic.getVersion()+1)
			.where(criteriaBuilder.and(
					criteriaBuilder.equal(root.get("id"), basic.getId()),
					criteriaBuilder.equal(root.get("version"), basic.getVersion())
					));
		
		int rows = entityManager.createQuery(criteriaUpdate).executeUpdate();
		
		if(rows == 0) throw new OptimisticLockException("Row with id " + basic.getId() + " was updated or deleted by another transaction");
	}

	@Override
	public long sumVersionByids(Iterable<Integer> ids) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<Subtask> root = criteriaQuery.from(Subtask.class);
		
		criteriaQuery.select(criteriaBuilder.sum(root.get("version")))
			.where(root.get("id").in(ids));
		
		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}

	@Override
	public long sumVersionByUser(int userId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<Subtask> root = criteriaQuery.from(Subtask.class);
		
		criteriaQuery.select(criteriaBuilder.sum(root.get("version")))
			.where(criteriaBuilder.equal(root.get("user").get("id"), userId));
		
		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}
	
}
