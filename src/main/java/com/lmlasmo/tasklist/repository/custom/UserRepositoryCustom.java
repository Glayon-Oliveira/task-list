package com.lmlasmo.tasklist.repository.custom;

import java.time.Instant;
import java.util.List;

import com.lmlasmo.tasklist.model.UserStatusType;
import com.lmlasmo.tasklist.repository.summary.UserSummary;

public interface UserRepositoryCustom {
	
	public List<UserSummary.StatusSummary> findStatusSummaryByStatus(UserStatusType status);
	
	public List<UserSummary.StatusSummary> findStatusSummaryByStatusAndLastLoginAfter(UserStatusType status, Instant after);

	public void changeStatusByIds(Iterable<Integer> ids, UserStatusType status);	
	
}
