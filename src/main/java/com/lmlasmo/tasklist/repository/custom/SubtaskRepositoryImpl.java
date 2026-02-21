package com.lmlasmo.tasklist.repository.custom;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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

import com.lmlasmo.tasklist.mapper.summary.SubtaskSummaryMapper;
import com.lmlasmo.tasklist.model.Subtask;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Repository
public class SubtaskRepositoryImpl extends RepositoryCustomImpl implements SubtaskRepositoryCustom {
	
	private R2dbcEntityTemplate template;
	private SubtaskSummaryMapper mapper;	
	
	@Override
	public Mono<Boolean> existsByIdAndTaskUserId(int subtaskId, int userId) {
		String sql = "SELECT EXISTS (%s) AS exists_result ";
		sql = String.format(sql, new StringBuilder()
				.append("SELECT 1 FROM subtasks s ")
				.append("JOIN tasks t ON s.task_id = t.id ")
				.append("WHERE s.id = ? ")
				.append("AND t.user_id = ?")
				);
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, subtaskId)
				.bind(1, userId)
				.map(row -> row.get("exists_result", Long.class))
				.one()
				.map(e -> e != null && e != 0L);
	}
	
	@Override
	public Mono<Long> countByTaskId(int taskId) {
		Query query = Query.query(Criteria.where("taskId").is(taskId));
		
		return template.count(query, Subtask.class);
	}

	@Override
	public Mono<Long> countByIdInAndTaskUserId(Collection<Integer> subtaskIds, int userId) {
		if(subtaskIds.isEmpty()) return Mono.just(0L);
		
		String clauses = subtaskIds.stream()
				.map(sid -> "?")
				.collect(Collectors.joining(", "));
		
		String sql = """
				SELECT COUNT(*) AS count FROM subtasks s
				JOIN tasks t ON s.task_id = t.id
				WHERE s.id IN (%s)
				AND t.user_id = ?
				""".formatted(clauses);
		
		List<Object> placeholders = new LinkedList<>();
		placeholders.addAll(subtaskIds);
		placeholders.add(userId);
		
		return template.getDatabaseClient()
				.sql(sql)
				.bindValues(placeholders)
				.map(row -> row.get("count", Long.class))
				.one();
	}
	
	@Override
	public Mono<SubtaskSummary> findSummaryById(int subtaskId, Set<String> includedFields) {
		Criteria criteria =  Criteria.where("id").is(subtaskId);
		
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		Query query = Query.query(criteria).columns(normalizedIncludedFields);
		
		return template.selectOne(query, Subtask.class)
				.map(st -> mapper.toSummary(st, normalizedIncludedFields));
	}
	
	@Override
	public Flux<SubtaskSummary> findSummariesByTaskId(int taskId, Pageable pageable, String contains, TaskStatusType status, Set<String> includedFields) {
		Criteria criteria = Criteria.where("taskId").is(taskId);
		
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
		
		return template.select(query, Subtask.class)
				.map(st -> mapper.toSummary(st, normalizedIncludedFields));
	}
	
	@Override
	public Flux<SubtaskSummary> findSummariesByTaskIdAndSort(int taskId, Sort sort, Set<String> includedFields) {
		Criteria criteria = Criteria.where("taskId").is(taskId);
		
		Sort normalizedSort = normalizerPropertiesOfSort(sort);
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		Query query = Query.query(criteria).sort(normalizedSort).columns(normalizedIncludedFields);
		
		return template.select(query, Subtask.class)
				.map(st -> mapper.toSummary(st, normalizedIncludedFields));
	}
	
	@Override
	public Flux<SubtaskSummary> findSummaryByRelatedSubtaskId(int subtaskId, Set<String> includedFields) {
		Set<String> normalizedIncludedFields = normalizeIncludedFieldsForSQL(includedFields)
				.stream()
				.map("s."::concat)
				.collect(Collectors.toSet());
		
		String sql = new StringBuilder("SELECT s.id, s.row_version, s.position, s.task_id FROM subtasks s ")
				.append("JOIN tasks t ON s.task_id = t.id ")
				.append("WHERE t.id = (SELECT task_id FROM subtasks WHERE id = ?) ")
				.append("AND s.id != ?")
				.toString()
				.formatted(
						normalizedIncludedFields.stream()
						.collect(Collectors.joining(", "))
						);
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, subtaskId)
				.bind(1, subtaskId)
				.map(r -> mapper.toSummary(r, normalizedIncludedFields))
				.all();
	}

	@Override
	public Flux<SubtaskSummary> findSummaryByTaskIdAndPositionGreaterThan(int taskId, BigDecimal position, Pageable pageable, Set<String> includedFields) {
		Criteria criteria = Criteria.where("taskId").is(taskId)
				.and(Criteria.where("position").greaterThan(position));
		
		Pageable normalizedPageable = normalizePropertiesOfPageable(pageable);
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		Query query = Query.query(criteria).columns(normalizedIncludedFields);
		
		if(normalizedPageable != null) query = query.with(normalizedPageable);
		
		return template.select(query, Subtask.class)
				.map(st -> mapper.toSummary(st, normalizedIncludedFields));
	}
	
	@Override
	public Flux<SubtaskSummary> findSummaryByTaskIdAndPositionLessThan(int taskId, BigDecimal position, Pageable pageable, Set<String> includedFields) {
		Criteria criteria = Criteria.where("taskId").is(taskId)
				.and(Criteria.where("position").lessThan(position));
		
		Pageable normalizedPageable = normalizePropertiesOfPageable(pageable);
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		Query query = Query.query(criteria).columns(normalizedIncludedFields);
		
		if(normalizedPageable != null) query = query.with(normalizedPageable);
		
		return template.select(query, Subtask.class)
				.map(st -> mapper.toSummary(st, normalizedIncludedFields));
	}

	@Override
	public Mono<Void> updatePriority(BasicSummary<Integer> basic, BigDecimal position) {
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
							SqlIdentifier.unquoted("position"), position,
							SqlIdentifier.unquoted("version"), version+1
							)),
					Subtask.class
					)
					.flatMap(l -> {
						return l == 1 ? Mono.empty() 
								: Mono.error(new OptimisticLockingFailureException("Row with id " + basic.getId() + " was updated or deleted by another transaction"));
					})
					.then()
					.as(getOperator()::transactional);
	}

	@Override
	public Mono<Void> updateStatus(BasicSummary<Integer> basic, TaskStatusType status) {
		if(!(basic.getId().isPresent() && basic.getVersion().isPresent())) {
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
						SqlIdentifier.unquoted("status"), status,
						SqlIdentifier.unquoted("version"), version+1
						)),
				Subtask.class
				)
				.flatMap(l -> {
					return l == 1 ? Mono.empty() 
							: Mono.error(new OptimisticLockingFailureException("Row with id " + basic.getId() + " was updated or deleted by another transaction"));
				}).then()
				.as(getOperator()::transactional);
	}

	@Override
	public Mono<Void> updateStatus(Collection<? extends BasicSummary<Integer>> basics, TaskStatusType status) {
		if(basics.isEmpty()) return Mono.empty();
		
		boolean valid = basics.stream()
				.allMatch(bs -> bs.getId().isPresent() && bs.getVersion().isPresent());
		
		if(!valid) throw new IllegalArgumentException("All the basic summaries must has present id and version");
		
		return Flux.fromIterable(basics)
				.flatMap(b -> {
					Integer id = b.getId().get();
					Long version = b.getVersion().get();
					
					return template.update(
							Query.query(
									Criteria.where("id").is(id)
									.and(Criteria.where("version").is(version))
									),
							Update.from(Map.of(
									SqlIdentifier.unquoted("status"), status,
									SqlIdentifier.unquoted("version"), version+1L
									)),
							Subtask.class
							)
							.flatMap(l -> {
								return l == 1
										? Mono.empty()
										: Mono.error(new OptimisticLockingFailureException("Row with id " + b.getId() + " was updated or deleted by another transaction"));
							});
				})
				.then()
				.as(getOperator()::transactional);
	}
	
	@Override
	public Mono<Void> updateStatusByTaskId(int taskId, TaskStatusType status) {
		Query query = Query.query(Criteria.where("task_id").is(taskId));
		
		Update update = Update.from(Map.of(
					SqlIdentifier.unquoted("status"), status
				));
		
		return template.update(query, update, Subtask.class)
				.then();
	}
	
	@Override
	public Mono<Long> sumVersionByids(Collection<Integer> ids) {
		if (ids.isEmpty()) return Mono.just(0L);
		
		String placeholders = ids.stream()
				.map(i -> "?")
				.collect(Collectors.joining(", "));
		
		String sql = "SELECT COALESCE(SUM(s.row_version), 0) FROM subtasks s WHERE id IN (%s)"
				.formatted(placeholders);
		
		return template.getDatabaseClient()
				.sql(sql)
				.bindValues(List.copyOf(ids))
				.map(row -> row.get(0, Long.class))
				.one();
	}
	
	@Override
	public Flux<SubtaskSummary> findSummaryByIds(Collection<Integer> subtaskIds, Set<String> includedFields) {
		Criteria criteria = Criteria.where("id").in(subtaskIds);
		
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		Query query = Query.query(criteria).columns(normalizedIncludedFields);
		
		return template.select(query, Subtask.class)
				.map(st -> mapper.toSummary(st, normalizedIncludedFields));
	}

	@Override
	public Mono<Long> sumVersionByTask(int taskId) {
		StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(s.row_version), 0) FROM subtasks s ")
				.append("JOIN tasks t ON s.task_id = t.id ")
				.append("WHERE t.id = ?");
		
		return template.getDatabaseClient()
				.sql(sql.toString())
				.bind(0, taskId)
				.map(row -> row.get(0, Long.class))
				.one();
	}
	
	@Override
	public Mono<Long> sumVersionByTask(int taskId, Pageable pageable, String contains, TaskStatusType status) {
		StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(s.row_version), 0) FROM subtasks s ")
				.append("JOIN tasks t ON s.task_id = t.id ")
				.append("WHERE t.id = ? ");
		
		if(status != null) {
			sql.append("AND s.status = '" + status.name() + "'");
		}
		
		if(contains != null && !contains.isBlank()) {
			String strLike= "%" + contains + "%";
			
			sql.append(" AND (s.name LIKE '" + strLike + "'")
			   	.append(" OR s.summary LIKE '" + strLike + "')");
		}
		
		sql.append(" LIMIT " + pageable.getPageSize())
	   		.append(" OFFSET " + pageable.getOffset());
		
		return template.getDatabaseClient()
				.sql(sql.toString())
				.bind(0, taskId)
				.map(row -> row.get(0, Long.class))
				.one();
	}
	
	private Set<String> normalizeIncludedFieldsForSQL(Set<String> includedFields) {
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		normalizedIncludedFields = normalizedIncludedFields.stream()
				.map(f -> f.replaceAll("([a-z]([A-Z]))", "$1_$2").toLowerCase())
				.map(f -> {
					return f.equals("version")
							? "row_version"
							: f;
				})
				.collect(Collectors.toSet());
		
		return normalizedIncludedFields;
	}
	
	private Set<String> normalizeIncludedFields(Set<String> includedFields) {
		Set<String> normalizedIncludedFields = new LinkedHashSet<>();
		normalizedIncludedFields.addAll(SubtaskSummary.REQUIRED_FIELDS);
		
		if(includedFields != null && includedFields.size() > 0) {
			Set<String> filtedFields = includedFields.stream()
					.map(String::trim)
					.filter(SubtaskSummary.FIELDS::contains)
					.collect(Collectors.toSet());
			
			normalizedIncludedFields.addAll(filtedFields);
		}else {
			normalizedIncludedFields.addAll(SubtaskSummary.FIELDS);
		}
		
		
		return normalizedIncludedFields;
	}
	
	private Pageable normalizePropertiesOfPageable(Pageable pageable) {
		if(pageable != null) {
			Sort normalizedSort = normalizerPropertiesOfSort(pageable.getSort());
			
			return PageRequest.of(
						pageable.getPageNumber(),
						pageable.getPageSize(),
						normalizedSort
					);
		}
		
		return null;
	}
	
	private Sort normalizerPropertiesOfSort(Sort sort) {
		Order[] orders = sort.stream()
			 	.filter(o -> SubtaskSummary.FIELDS.contains(o.getProperty()))
			 	.toArray(Order[]::new);
		
		return Sort.by(orders);
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
