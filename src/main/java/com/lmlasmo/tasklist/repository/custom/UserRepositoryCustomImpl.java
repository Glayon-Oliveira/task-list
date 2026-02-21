package com.lmlasmo.tasklist.repository.custom;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lmlasmo.tasklist.mapper.UserMapper;
import com.lmlasmo.tasklist.mapper.summary.UserSummaryMapper;
import com.lmlasmo.tasklist.model.User;
import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.summary.UserSummary;

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
	public Mono<UserSummary> findSummaryByEmail(String email, Set<String> includedFields) {
		Set<String> normalizedIncludedFields = normalizeIncludedFieldsForSQL(includedFields)
				.stream()
				.map("u."::concat)
				.collect(Collectors.toSet());
				
		String sql = new StringBuilder("SELECT %s FROM users u ")
				.append("JOIN user_emails um ON um.user_id = u.id ")
				.append("WHERE um.email = ?")
				.toString()
				.formatted(
						normalizedIncludedFields.stream()
						.collect(Collectors.joining(", "))
						);
				
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, email)
				.map(r -> summaryMapper.toSummary(r, normalizedIncludedFields))
				.one();
	}
	
	@Override
	public Flux<UserSummary> findSummaryByStatus(UserStatusType status, Set<String> includedFields) {
		Criteria criteria = Criteria.where("status").is(status);
		
		Set<String> normalizedIncludedFields = normalizeIncludedFieldsForSQL(includedFields);
		
		Query query = Query.query(criteria).columns(normalizedIncludedFields);
		
		return template.select(query, User.class)
				.map(u -> summaryMapper.toSummary(u, normalizedIncludedFields));
	}

	@Override
	public Flux<UserSummary> findSummaryByStatusAndLastLoginAfter(UserStatusType status, Instant after, Set<String> includedFields) {
		Set<String> normalizedIncludedFields = normalizeIncludedFieldsForSQL(includedFields);
		
		String sql = "SELECT FROM users WHERE status = ? AND last_login > ?"
				.formatted(
						normalizedIncludedFields.stream()
						.collect(Collectors.joining(", "))
						);
				
		
		return template.getDatabaseClient()
				.sql(sql)
				.bind(0, status.toString())
				.bind(1, after)
				.map(r -> summaryMapper.toSummary(r, normalizedIncludedFields))
				.all();
	}
	
	private Set<String> normalizeIncludedFields(Set<String> includedFields) {
		Set<String> normalizedIncludedFields = new LinkedHashSet<>();
		normalizedIncludedFields.addAll(UserSummary.REQUIRED_FIELDS);
		
		if(includedFields != null && includedFields.size() > 0) {
			Set<String> filtedFields = includedFields.stream()
					.map(String::trim)
					.filter(UserSummary.FIELDS::contains)
					.collect(Collectors.toSet());
			
			normalizedIncludedFields.addAll(filtedFields);
		}else {
			normalizedIncludedFields.addAll(UserSummary.FIELDS);
		}
		
		
		return normalizedIncludedFields;
	}
	
	private Set<String> normalizeIncludedFieldsForSQL(Set<String> includedFields) {
		Set<String> normalizedIncludedFields = normalizeIncludedFields(includedFields);
		
		normalizedIncludedFields = normalizedIncludedFields.stream()
				.map(f -> f.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase())
				.map(f -> {
					return f.equals("version")
							? "row_version"
							: f;
				})
				.collect(Collectors.toSet());
		
		return normalizedIncludedFields;
	}

	@Override
	public Mono<Void> changeStatusByIds(Collection<Integer> userIds, UserStatusType status) {
		if(userIds.isEmpty()) return Mono.empty();
		
		return template.update(
					Query.query(Criteria.where("id").in(userIds)),
					Update.update("status", status),
					User.class
				).then()
				.as(getOperator()::transactional);
	}
	
}
