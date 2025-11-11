package com.lmlasmo.tasklist.advice.exception.util;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;

public interface AdviceWrapper {
	
	public static Map<String, Object> wrapper(ServerHttpRequest req){
		return wrapper(HttpStatus.BAD_REQUEST, req);
	}
	
	public static Map<String, Object> wrapper(HttpStatus status, ServerHttpRequest req) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("timestamp", Instant.now());
		map.put("status", status.value());
		map.put("error", status.getReasonPhrase());
		map.put("path", req.getURI());
		return map;
	}
	
	public static Map<String, Object> wrapper(String message, ServerHttpRequest req){
		return wrapper(HttpStatus.BAD_REQUEST, message, req);
	}

	public static Map<String, Object> wrapper(HttpStatus status, String message, ServerHttpRequest req) {
		Map<String, Object> map = wrapper(status, req);
		map.put("message", message);
		return map;
	}

	public static Map<String, Object> wrapper(ServerHttpRequest req, Map<String, Object> fields) {
		return wrapper(HttpStatus.BAD_REQUEST, req, fields);
	}
	
	public static Map<String, Object> wrapper(HttpStatus status, ServerHttpRequest req, Map<String, Object> fields) {
		Map<String, Object> map = wrapper(status, req);
		map.putAll(fields);
		return map;
	}
	
	public static Map<String, Object> wrapper(String message, ServerHttpRequest req, Map<String, Object> fields) {
		return wrapper(HttpStatus.BAD_REQUEST, message, req, fields);
	}

	public static Map<String, Object> wrapper(HttpStatus status, String message, ServerHttpRequest req, Map<String, Object> fields) {
		Map<String, Object> map = wrapper(status, message, req);
		map.putAll(fields);
		return map;
	}
	
}
