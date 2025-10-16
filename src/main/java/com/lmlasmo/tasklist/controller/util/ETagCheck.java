package com.lmlasmo.tasklist.controller.util;

import java.util.function.Function;

import com.lmlasmo.tasklist.exception.PreconditionFailedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ETagCheck {		
	
	public static boolean check(HttpServletRequest req, HttpServletResponse res, Function<Long, Boolean> check) {
		long etag = ETagCheck.extractEtag(req);
		
		if(etag <= 0) return false;
		
		if(req.getMethod().equals("PUT")) {
			checkForPut(etag, check);
			return true;
		}else {
			return check(etag, res, check);
		}
	}
	
	private static void checkForPut(long etag, Function<Long, Boolean> check) {
		if (!check.apply(etag)) {
            throw new PreconditionFailedException("");
        }
	}

	private static boolean check(Long etag, HttpServletResponse res, Function<Long, Boolean> check) {
		boolean valid = check.apply(etag);
		if (valid) res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		return valid;
    }
	
	private static long extractEtag(HttpServletRequest request) {
		String ifNoneMatch = request.getHeader("If-None-Match");
		
		if(ifNoneMatch == null) return -1;
		
		if(ifNoneMatch.contains("\"")) ifNoneMatch = ifNoneMatch.replace("\"", "");
		
		try {
			return Long.parseLong(ifNoneMatch);
		}catch(Exception e) {
			return -1;
		}
	}
	
}
