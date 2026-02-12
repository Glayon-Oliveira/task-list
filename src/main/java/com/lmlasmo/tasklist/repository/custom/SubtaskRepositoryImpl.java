package com.lmlasmo.tasklist.repository.custom;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
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
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.PositionSummary;
import com.lmlasmo.tasklist.repository.summary.SubtaskSummary.StatusSummary;

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
				.map(row -> row.get("exists_result", Boolean.class))
				.one();
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
	
	public Flux<Subtask> findAllByTaskId(int taskId, Pageable pageable, String contains, TaskStatusType status) {
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
		
		return template.select(query, Subtask.class);
	}
	
	@Override
	public Flux<SubtaskSummary> findAllByTaskId(int taskId, Pageable pageable, String contains, TaskStatusType status, String... fields) {
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
		
		if(fields != null && fields.length > 0) {
			Set<String> requiredFields = Set.of("id", "version", "createdAt", "updatedAt");
			
			Set<String> filtedFields = Arrays.stream(fields)
					.map(String::trim)
					.filter(SubtaskSummary.FIELDS::contains)
					.collect(Collectors.toSet());
			
			query = query.columns(requiredFields).columns(filtedFields);
		}
		
		return template.select(query, SubtaskSummary.class);
	}
	
	private Pageable normalizePropertiesOfPageable(Pageable pageable) {
		if(pageable != null) {
			Order[] orders = pageable.getSort()
				 	.stream()
				 	.filter(o -> SubtaskSummary.FIELDS.contains(o.getProperty()))
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

	@Override	
	public Flux<PositionSummary> findPositionSummaryByTaskIdOrderByASC(int taskId) {
		String sql = new StringBuilder("SELECT s.id, s.row_version, s.position, s.task_id FROM subtasks s ")
				.append("JOIN tasks t ON s.task_id = t.id ")
				.append("WHERE t.id = ? ")
				.append("ORDER BY s.position ")
				.toString();
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, taskId)
				.map(mapper::toPositionSummary)
				.all();
	}

	@Override
	public Mono<PositionSummary> findPositionSummaryById(int subtaskId) {
		String sql = "SELECT id, row_version, position, task_id FROM subtasks WHERE id = ?";
		
		return template.getDatabaseClient()
				.sql(sql.toString())
				.bind(0, subtaskId)
				.map(mapper::toPositionSummary)
				.one();
	}

	@Override
	public Flux<PositionSummary> findPositionSummaryByRelatedSubtaskId(int subtaskId) {
		String sql = new StringBuilder("SELECT s.id, s.row_version, s.position, s.task_id FROM subtasks s ")
				.append("JOIN tasks t ON s.task_id = t.id ")
				.append("WHERE t.id = (SELECT task_id FROM subtasks WHERE id = ?) ")
				.append("AND s.id != ?")
				.toString();
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, subtaskId)
				.bind(1, subtaskId)
				.map(mapper::toPositionSummary)
				.all();
	}
	
	public Mono<PositionSummary> findFirstPositionSummaryByTaskIdOrderByASC(int taskId) {
		String sql = "SELECT id, row_version, position, task_id FROM subtasks WHERE task_id = ? ORDER BY position ASC LIMIT 1";
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, taskId)
				.map(mapper::toPositionSummary)
				.first();
	}
	
	public Mono<PositionSummary> findLastPositionSummaryByTaskIdOrderByASC(int taskId) {
		String sql = "SELECT id, row_version, position, task_id FROM subtasks WHERE task_id = ? ORDER BY position DESC LIMIT 1";
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, taskId)
				.map(mapper::toPositionSummary)
				.first();
	}
	
	@Override
	public Mono<PositionSummary> findFirstPositionSummaryByTaskIdAndPositionGreaterThanOrderByASC(int taskId, BigDecimal position) {
		String sql = "SELECT id, row_version, position, task_id FROM subtasks WHERE task_id = ? AND position > ? ORDER BY position ASC LIMIT 1";
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, taskId)
				.bind(1, position)
				.map(mapper::toPositionSummary)
				.first();
	}

	@Override
	public Mono<PositionSummary> findFirstPositionSummaryByTaskIdAndPositionLessThanOrderByDESC(int taskId, BigDecimal position) {
		String sql = "SELECT id, row_version, position, task_id FROM subtasks WHERE task_id = ? AND position < ? ORDER BY position DESC LIMIT 1";
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, taskId)
				.bind(1, position)
				.map(mapper::toPositionSummary)
				.first();
	}

	@Override
	public Mono<Void> updatePriority(BasicSummary basic, BigDecimal position) {
		return template.update(
					Query.query(
							Criteria.where("id").is(basic.getId())
							.and(Criteria.where("version").is(basic.getVersion()))
							),
					Update.from(Map.of(
							SqlIdentifier.unquoted("position"), position,
							SqlIdentifier.unquoted("version"), basic.getVersion()+1
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
	public Mono<StatusSummary> findStatusSummaryById(int subtaskId) {
		String sql = new StringBuilder("SELECT s.id, s.row_version, s.status, s.task_id FROM subtasks s ")
				.append("JOIN tasks t ON s.task_id = t.id ")
				.append("WHERE s.id = ?")
				.toString();
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, subtaskId)
				.map(mapper::toStatusSummary)
				.one();
	}

	@Override
	public Flux<StatusSummary> findStatusSummaryByIds(Collection<Integer> subtaskIds) {
		if(subtaskIds.isEmpty()) return Flux.empty();
		
		String placeholders = subtaskIds.stream()
				.map(i -> "?")
				.collect(Collectors.joining(", "));
		
		String sql = new StringBuilder("SELECT s.id, s.row_version, s.status, t.id AS task_id FROM subtasks s ")
				.append("JOIN tasks t ON s.task_id = t.id ")
				.append("WHERE s.id IN (%s)")
				.toString()
				.formatted(placeholders);
		
		
		
		return template.getDatabaseClient()
				.sql(sql)
				.bindValues(List.copyOf(subtaskIds))
				.map(mapper::toStatusSummary)
				.all();
	}

	@Override
	public Mono<Void> updateStatus(BasicSummary basic, TaskStatusType status) {
		return template.update(
				Query.query(
						Criteria.where("id").is(basic.getId())
						.and(Criteria.where("version").is(basic.getVersion()))
						),
				Update.from(Map.of(
						SqlIdentifier.unquoted("status"), status,
						SqlIdentifier.unquoted("version"), basic.getVersion()+1
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
	public Mono<Void> updateStatus(Collection<? extends BasicSummary> basics, TaskStatusType status) {
		if(basics.isEmpty()) return Mono.just(null);
		
		return Flux.fromIterable(basics)
				.flatMap(b -> {
					return template.update(
							Query.query(
									Criteria.where("id").is(b.getId())
									.and(Criteria.where("version").is(b.getVersion()))
									),
							Update.from(Map.of(
									SqlIdentifier.unquoted("status"), status,
									SqlIdentifier.unquoted("version"), b.getVersion()+1
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
	public Mono<Long> sumVersionByids(Collection<Integer> ids) {
		if (ids.isEmpty()) return Mono.just(0L);
		
		String placeholders = ids.stream()
				.map(i -> "?")
				.collect(Collectors.joining(", "));
		
		String sql = "SELECT COALESCE(CAST(SUM(s.row_version) AS BIGINT), 0) FROM subtasks s WHERE id IN (%s)"
				.formatted(placeholders);
		
		return template.getDatabaseClient()
				.sql(sql)
				.bindValues(List.copyOf(ids))
				.map(row -> row.get(0, Long.class))
				.one();
	}

	@Override
	public Mono<Long> sumVersionByTask(int taskId) {
		StringBuilder sql = new StringBuilder("SELECT COALESCE(CAST(SUM(s.row_version) AS BIGINT), 0) FROM subtasks s ")
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
		StringBuilder sql = new StringBuilder("SELECT COALESCE(CAST(SUM(s.row_version) AS BIGINT), 0) FROM subtasks s ")
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

}
