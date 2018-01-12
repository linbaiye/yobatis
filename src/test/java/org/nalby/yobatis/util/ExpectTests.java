package org.nalby.yobatis.util;

import static org.junit.Assert.fail;

import org.junit.Test;

public class ExpectTests {
	
	@Test(expected = IllegalArgumentException.class)
	public void expectNotNull() {
		try {
			Expect.notNull(null, null);
		} catch (IllegalArgumentException e) {
		}
		Expect.notNull(new String(), null);
		Expect.notNull(null, "");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void expectNotEmpty() {
		Expect.notEmpty("not empty", null);
		try {
			Expect.notEmpty("", null);
			fail();
		} catch (IllegalArgumentException e) {
		}
		Expect.notEmpty("", "msg");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void asTrue() {
		Expect.asTrue(true, null);
		try {
			Expect.asTrue(false, null);
			fail();
		} catch (IllegalArgumentException e) {
		}
		Expect.asTrue(false, "msg");
	}


}
