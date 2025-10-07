package com.lmlasmo.tasklist.repository.custom;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class TaskRepositoryImpl implements TaskRepositoryCustom{

	private EntityManager entityManager;

	@Override
	@Transactional
	public void updateStatus(int taskId, TaskStatusType status) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaUpdate<Task> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(Task.class);
		Root<Task> root = criteriaUpdate.from(Task.class);
		
		criteriaUpdate.set(root.get("status"), status)
			.where(criteriaBuilder.equal(root.get("id"), taskId));
		
		entityManager.createQuery(criteriaUpdate).executeUpdate();
	}
	
	
	
}
