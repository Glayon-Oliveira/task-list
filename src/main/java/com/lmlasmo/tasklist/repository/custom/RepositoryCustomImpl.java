package com.lmlasmo.tasklist.repository.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.Getter;

@Repository
public class RepositoryCustomImpl implements RepositoryCustom {

	@Getter
	@Autowired
	private TransactionalOperator operator;
	
}
