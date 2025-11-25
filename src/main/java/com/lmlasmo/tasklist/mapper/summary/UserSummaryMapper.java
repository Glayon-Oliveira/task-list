package com.lmlasmo.tasklist.mapper.summary;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.mapstruct.Mapper;

import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.summary.UserSummary.StatusSummary;

import io.r2dbc.spi.Readable;

@Mapper(componentModel = "spring")
public interface UserSummaryMapper {
	
	default StatusSummary toStatusSummary(int id, long version, UserStatusType status, Instant lastLogin) {
		return new StatusSummary() {
			@Override
			public int getId() {return id;}
			
			@Override
			public long getVersion() {return version;}
			
			@Override
			public UserStatusType getStatus() {return status;}
			
			@Override
			public Instant getLastLogin() {return lastLogin;}
		};
	}
	
	default StatusSummary toStatusSummary(Readable row) {
		return toStatusSummary(
				row.get("id", Integer.class),
				row.get("row_version", Long.class),
				UserStatusType.valueOf(row.get("status", String.class)),
				Optional.ofNullable(row.get("last_login", LocalDateTime.class))
					.map(i -> i.toInstant(ZoneOffset.UTC))
					.orElse(null)
				);
	}
	
}
