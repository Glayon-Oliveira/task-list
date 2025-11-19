package com.lmlasmo.tasklist.repository.custom;

import org.springframework.transaction.reactive.TransactionalOperator;

public interface RepositoryCustom {

	public TransactionalOperator getOperator();
	
}
