package com.lmlasmo.tasklist.repository.custom;

import java.time.Instant;
import java.util.Collection;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lmlasmo.tasklist.mapper.UserMapper;
import com.lmlasmo.tasklist.mapper.summary.UserSummaryMapper;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.summary.UserSummary.StatusSummary;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Repository
public class UserRepositoryCustomImpl extends RepositoryCustomImpl implements UserRepositoryCustom {
	
	private R2dbcEntityTemplate template;
	private UserMapper mapper;
	private UserSummaryMapper summaryMapper;
	
	@Override
	public Mono<User> findByEmail(String email) {
		String sql = new StringBuilder("SELECT u.* FROM users u ")
				.append("JOIN user_emails um ON um.user_id = u.id ")
				.append("WHERE um.email = ?")
				.toString();
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, email)
				.map(mapper::toUser)
				.one();
	}
	
	@Override
	public Flux<StatusSummary> findStatusSummaryByStatus(UserStatusType status) {
		String sql = "SELECT id, row_version, status, last_login FROM users WHERE status = ?";
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, status.toString())
				.map(summaryMapper::toStatusSummary)
				.all();
	}

	@Override
	public Flux<StatusSummary> findStatusSummaryByStatusAndLastLoginAfter(UserStatusType status, Instant after) {
		String sql = "SELECT id, row_version, status, last_login FROM users WHERE status = ? AND last_login > ?";
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, status.toString())
				.bind(1, after)
				.map(summaryMapper::toStatusSummary)
				.all();
	}

	@Override
	public Mono<Void> changeStatusByIds(Collection<Integer> userIds, UserStatusType status) {
		if(userIds.isEmpty()) return Mono.just(null);
		
		return template.update(
					Query.query(Criteria.where("id").in(userIds)),
					Update.update("status", status),
					User.class
				).then()
				.as(getOperator()::transactional);
	}
	
}
