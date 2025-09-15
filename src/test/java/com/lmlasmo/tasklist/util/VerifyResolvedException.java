package com.lmlasmo.tasklist.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.test.web.servlet.MvcResult;

public interface VerifyResolvedException {

	public static void verify(MvcResult mvcResult, Class<? extends Exception> expectedException) {
		Exception resolvedException = mvcResult.getResolvedException();

		if(resolvedException == null && expectedException == null) return;

		assertNotNull(resolvedException);
		assertTrue(expectedException.isInstance(resolvedException));
	}

}
