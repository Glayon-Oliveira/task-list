package com.lmlasmo.tasklist.repository.custom;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
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
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

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
	public Flux<Task> findAllByUserId(int userId, Pageable pageable, String contains, TaskStatusType status) {
		Criteria criteria = Criteria.where("userId").is(userId);
		
		if(status != null) {
			criteria = criteria.and(Criteria.where("status").is(status));
		}
		
		if(contains != null && !contains.isBlank()) {
			String strLike = "%" + contains + "%";
			
			criteria = criteria.and(
					Criteria.where("name").like(strLike)
					.or(Criteria.where("summary").like(strLike))
					);
		}
		
		Query query = Query.query(criteria).with(pageable);
		return template.select(query, Task.class);
	}
	
	@Override
	public Mono<StatusSummary> findStatusSummaryById(int taskId) {
		String sql = "SELECT id, row_version, status FROM tasks WHERE id = ?";
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, taskId)
				.map(mapper::toStatusSummary)
				.one();
	}

	@Override	
	public Mono<Void> updateStatus(BasicSummary basic, TaskStatusType status) {
		return template.update(
				Query.query(
						Criteria.where("id").is(basic.getId())
						.and(Criteria.where("version").is(basic.getVersion()))
						),
				Update.from(Map.of(
						SqlIdentifier.unquoted("status"), status.toString(),
						SqlIdentifier.unquoted("version"), basic.getVersion()+1
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
		
		String sql = "SELECT COALESCE(CAST(SUM(t.row_version) AS BIGINT), 0) FROM tasks t WHERE id IN (%s)"
				.formatted(placeholders);
		
		return template.getDatabaseClient()
				.sql(sql)
				.bindValues(List.copyOf(ids))
				.map(row -> row.get(0, Long.class))
				.one();
	}

	@Override
	public Mono<Long> sumVersionByUser(int userId) {
		String sql = new StringBuilder("SELECT COALESCE(CAST(SUM(t.row_version) AS BIGINT), 0) FROM tasks t ")
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
		StringBuilder sql = new StringBuilder("SELECT COALESCE(CAST(SUM(t.row_version) AS BIGINT), 0) FROM tasks t ")
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
	
}
