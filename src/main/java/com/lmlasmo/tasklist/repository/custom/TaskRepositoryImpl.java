package com.lmlasmo.tasklist.repository.custom;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Repository;

import com.lmlasmo.tasklist.mapper.summary.TaskSummaryMapper;
import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Repository
public class TaskRepositoryImpl extends RepositoryCustomImpl implements TaskRepositoryCustom {

	private R2dbcEntityTemplate template;
	private TaskSummaryMapper mapper;
	
	@Override
	public Mono<Long> countByUserId(int userId) {
		Query query = Query.query(Criteria.where("userId").is(userId));
		return template.count(query, Task.class);
	}
	
	@Override
	public Mono<TaskSummary> findSummaryById(int id, Set<String> includedFields) {
		Criteria criteria = Criteria.where("id").is(id);
		
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		Query query = Query.query(criteria).columns(normalizedIncludedFields);
		
		return template.selectOne(query, Task.class)
				.map(t -> mapper.toSummary(t, normalizedIncludedFields));
	}
	
	@Override
	public Flux<TaskSummary> findSummariesByUserId(int userId, Pageable pageable, String contains, TaskStatusType status, Set<String> includedFields) {
		Criteria criteria = Criteria.where("userId").is(userId);
		
		if(status != null) {
			criteria = criteria.and(Criteria.where("status").is(status));
		}
		
		criteria = buildCriteriaWithContains(criteria, contains);
		
		Query query = Query.query(criteria);
		Pageable normalizedPageable = normalizePropertiesOfPageable(pageable);
		
		if(normalizedPageable != null) {
			query = query.with(normalizedPageable);
		}
		
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		query = query.columns(normalizedIncludedFields);
		
		return template.select(query, Task.class)
				.map(t -> mapper.toSummary(t, normalizedIncludedFields));
	}

	@Override	
	public Mono<Void> updateStatus(BasicSummary<Integer> basic, TaskStatusType status) {
		if((!basic.getId().isPresent() && basic.getVersion().isPresent())) {
			throw new IllegalArgumentException("Basic summary must has present id and version");
		}
		
		Integer id = basic.getId().get();
		Long version = basic.getVersion().get();
		
		return template.update(
				Query.query(
						Criteria.where("id").is(id)
						.and(Criteria.where("version").is(version))
						),
				Update.from(Map.of(
						SqlIdentifier.unquoted("status"), status.toString(),
						SqlIdentifier.unquoted("version"), version+1
						)),
				Task.class
				)
				.flatMap(l -> {
					return l > 0 ? Mono.empty() 
							: Mono.error(new OptimisticLockingFailureException("Row with id " + basic.getId() + " was updated or deleted by another transaction"));
				}).then()
				.as(getOperator()::transactional);
	}

	@Override
	public Mono<Long> sumVersionByids(Collection<Integer> ids) {
		if(ids.isEmpty()) return Mono.just(0L);
		
		String placeholders = ids.stream()
				.map(i -> "?")
				.collect(Collectors.joining(", "));
		
		String sql = "SELECT COALESCE(SUM(t.row_version), 0) FROM tasks t WHERE id IN (%s)"
				.formatted(placeholders);
		
		return template.getDatabaseClient()
				.sql(sql)
				.bindValues(List.copyOf(ids))
				.map(row -> row.get(0, Long.class))
				.one();
	}

	@Override
	public Mono<Long> sumVersionByUser(int userId) {
		String sql = new StringBuilder("SELECT COALESCE(SUM(t.row_version), 0) FROM tasks t ")
				.append("JOIN users u ON t.user_id = u.id ")
				.append("WHERE u.id = ?")
				.toString();
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, userId)
				.map(row -> row.get(0, Long.class))
				.one();
	}
	
	@Override
	public Mono<Long> sumVersionByUser(int userId, Pageable pageable, String contains, TaskStatusType status) {
		StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(t.row_version), 0) FROM tasks t ")
				.append("JOIN users u ON t.user_id = u.id ")
				.append("WHERE u.id = ? ");
		
		if(status != null) {
			sql.append("AND t.status = '" + status.name() + "'");
		}
		
		if(contains != null && !contains.isBlank()) {
			String strLike= "%" + contains + "%";
			
			sql.append(" AND (t.name LIKE '" + strLike + "'")
			   	.append(" OR t.summary LIKE '" + strLike + "')");
		}
		
		sql.append(" LIMIT " + pageable.getPageSize())
	   		.append(" OFFSET " + pageable.getOffset());
		
		return template.getDatabaseClient()
				.sql(sql.toString())
				.bind(0, userId)
				.map(row -> row.get(0, Long.class))
				.one();
	}
	
	private Set<String> normalizeIncludedFields(Set<String> includedFields) {
		Set<String> normalizedIncludedFields = new LinkedHashSet<>();
		normalizedIncludedFields.addAll(TaskSummary.REQUIRED_FIELDS);
		
		if(includedFields != null && includedFields.size() > 0) {
			Set<String> filtedFields = includedFields.stream()
					.map(String::trim)
					.filter(TaskSummary.FIELDS::contains)
					.collect(Collectors.toSet());
			
			normalizedIncludedFields.addAll(filtedFields);
		}else {
			normalizedIncludedFields.addAll(TaskSummary.FIELDS);
		}
		
		
		return normalizedIncludedFields;
	}
	
	private Pageable normalizePropertiesOfPageable(Pageable pageable) {
		if(pageable != null) {
			Order[] orders = pageable.getSort()
				 	.stream()
				 	.filter(o -> TaskSummary.FIELDS.contains(o.getProperty()))
				 	.toArray(Order[]::new);
			
			return PageRequest.of(
						pageable.getPageNumber(),
						pageable.getPageSize(),
						Sort.by(orders)
					);
		}
		
		return null;
	}
	
	private Criteria buildCriteriaWithContains(Criteria criteria, String contains) {
		if(contains != null && !contains.isBlank()) {
			String strLike = "%" + contains + "%";
			
			return criteria.and(
						Criteria.where("name").like(strLike)
						.or(Criteria.where("summary").like(strLike))
					);
		}
		
		return criteria;
	}
	
}
