package org.nalby.yobatis.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TextUtilTests {
	
	@Test
	public void test() {
		assertTrue(TextUtil.isEmpty(null));
		assertTrue(TextUtil.isEmpty(" "));
		assertTrue(TextUtil.isEmpty("    "));
		assertFalse(TextUtil.isEmpty("  x  "));
	}

}
