package org.nalby.yobatis.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AntPathMatcherTests {
	
	
	@Test
	public void test() {
		AntPathMatcher matcher = new AntPathMatcher();
		assertTrue(matcher.match("/src/**", "/src/test/java"));

		assertTrue(matcher.match("/src/?", "/src/x"));

		assertFalse(matcher.match("/src/?", "/src/xx"));

		assertTrue(matcher.match("/**/test", "/xx/test"));

		assertTrue(matcher.match("/**/test", "/xx/aa/test"));

		assertTrue(matcher.match("/**/test", "/xx/aa/sss/test"));

	}
	

}
