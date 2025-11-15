package com.lmlasmo.tasklist.repository.custom;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Repository;

import com.lmlasmo.tasklist.model.Task;
import com.lmlasmo.tasklist.model.TaskStatusType;
import com.lmlasmo.tasklist.repository.summary.BasicSummary;
import com.lmlasmo.tasklist.repository.summary.TaskSummary.StatusSummary;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Repository
public class TaskRepositoryImpl extends RepositoryCustomImpl implements TaskRepositoryCustom {

	private R2dbcEntityTemplate template;
	
	@Override
	public Mono<StatusSummary> findStatusSummaryById(int taskId) {
		String sql = "SELECT id, row_version, status FROM tasks WHERE id = ?";
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, taskId)
				.map((row, meta) -> new StatusSummary(
						row.get("id", Integer.class),
						row.get("row_version", Long.class),
						TaskStatusType.valueOf(row.get("status", String.class))
						))
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
		
		String sql = "SELECT COALESCE(SUM(t.row_version), 0) FROM tasks WHERE id IN (%s)"
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
	
}
