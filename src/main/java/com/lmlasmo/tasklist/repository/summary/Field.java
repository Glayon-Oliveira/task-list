package com.lmlasmo.tasklist.repository.summary;

import java.util.NoSuchElementException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Field<T> {

	@Getter private boolean present;
	private T value;
	
	public static <T> Field<T> absent() {
		return new Field<T>(false, null);
	}
	
	public static <T> Field<T> of(T value) {
		return new Field<T>(true, value);
	}
	
	public T get() {
		if(!this.present) throw new NoSuchElementException("This field was not provided");
		
		return value;
	}
	
}
