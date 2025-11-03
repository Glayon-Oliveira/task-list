package com.lmlasmo.tasklist.repository.custom;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.summary.UserSummary.StatusSummary;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
	
	private EntityManager manager;
	
	@Override
	@Transactional(readOnly = true)
	public List<StatusSummary> findStatusSummaryByStatus(UserStatusType status) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<StatusSummary> query = builder.createQuery(StatusSummary.class);
		Root<User> root = query.from(User.class);
		
		query.select(builder.construct(
				StatusSummary.class, 
				root.get("id"),
				root.get("version"),
				root.get("status"),
				root.get("lastLogin")))
		.where(builder.equal(root.get("status"), status));
		
		return manager.createQuery(query).getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<StatusSummary> findStatusSummaryByStatusAndLastLoginAfter(UserStatusType status, Instant after) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<StatusSummary> query = builder.createQuery(StatusSummary.class);
		Root<User> root = query.from(User.class);
		
		query.select(builder.construct(
				StatusSummary.class, 
				root.get("id"),
				root.get("version"),
				root.get("status"),
				root.get("lastLogin")))
		.where(builder.and(
				builder.equal(root.get("status"), status),
				builder.greaterThan(root.get("lastLogin"), after)));
		
		return manager.createQuery(query).getResultList();
	}

	@Override
	@Transactional
	public void changeStatusByIds(Iterable<Integer> userIds, UserStatusType status) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaUpdate<User> update = builder.createCriteriaUpdate(User.class);
		Root<User> root = update.from(User.class);
		
		List<Integer> ids = new ArrayList<>();
		userIds.forEach(ids::add);
		
		update.set(root.get("status"), status)
			.where(root.get("id").in(new ArrayList<Integer>(ids)));
		
		manager.createQuery(update).executeUpdate();
	}	
	
}
