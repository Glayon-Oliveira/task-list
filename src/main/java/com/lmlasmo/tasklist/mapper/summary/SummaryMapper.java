package com.lmlasmo.tasklist.mapper.summary;

import java.util.Set;

import com.lmlasmo.tasklist.repository.summary.Field;

public interface SummaryMapper {

	default <T> Field<T> unwrap(String fieldName, T fieldValue, Set<String> includedFields) {
		if(includedFields.contains(fieldName)) {
			return Field.of(fieldValue);
		}
		
		return Field.absent();
	}
	
}
