package com.lmlasmo.tasklist.repository.summary;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public interface Summary<T> {
	
	public static Set<String> REQUIRED_FIELDS = Set.of("id", "version", "createdAt", "updatedAt");
	
	public Field<T> getId();
	
	public Field<Long> getVersion();
	
	public Field<Instant> getCreatedAt();
	
	public Field<Instant> getUpdatedAt();
	
	public default Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		
		for(java.lang.reflect.Field field: this.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			
			try {
				Object value = field.get(this);
				
				if(value instanceof Field<?> wrapper) {
					if(wrapper.isPresent()) {
						map.put(field.getName(), wrapper.get());
					}
				}
				
			}catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}
		
		return map;
	}
}
