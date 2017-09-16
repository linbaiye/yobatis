package org.nalby.yobatis.util;

public final class Expect {
	private Expect() {}

	public static void asTrue(boolean condition, String errMsg) {
		if (!condition) {
			throw new IllegalArgumentException(errMsg == null ? "condition is supposed to be true." : errMsg);
		}
	}

	public static void notNull(Object object, String errMsg) {
		if (object == null) {
			throw new IllegalArgumentException(errMsg == null ? "null potintor passed." : errMsg);
		}
	}

	public static void notEmpty(String object, String errMsg) {
		notNull(object, errMsg);
		if ("".equals(object.trim())) {
			throw new IllegalArgumentException(errMsg == null ? "Empty string passed." : errMsg);
		}
	}
}
