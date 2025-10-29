package com.lmlasmo.tasklist.controller.util;

import java.util.Set;
import java.util.function.Function;

import com.lmlasmo.tasklist.exception.PreconditionFailedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ETagCheck {
	
	private static Set<String> getIfMatchMathods() {
		return Set.of("PUT", "PATCH", "DELETE");
	}
	
	public static boolean check(HttpServletRequest req, HttpServletResponse res, Function<Long, Boolean> check) {
		long etag = ETagCheck.extractEtag(req);
		
		if(etag <= 0) return false;
		
		if(getIfMatchMathods().contains(req.getMethod())) {
			return checkPrecondition(etag, check);
		}else if(req.getMethod().equals("GET")) {
			return checkIfModified(etag, res, check);
		}
		
		return false;
	}
	
	private static boolean checkPrecondition(long etag, Function<Long, Boolean> check) {
		if (!check.apply(etag)) throw new PreconditionFailedException("");
		
		return true;
	}

	private static boolean checkIfModified(Long etag, HttpServletResponse res, Function<Long, Boolean> check) {
		boolean valid = check.apply(etag);
		if (valid) res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		return valid;
    }
	
	private static long extractEtag(HttpServletRequest req) {
		String match = null;
		
		if(getIfMatchMathods().contains(req.getMethod())) {
			match = req.getHeader("If-Match");
		}else if(req.getMethod().equals("GET")) {
			match = req.getHeader("If-None-Match");
		}		
		
		if(match == null) return -1;
		
		if(match.contains("\"")) match = match.replace("\"", "").trim();
		
		try {
			return Long.parseLong(match);
		}catch(Exception e) {
			return -1;
		}
	}
	
}
