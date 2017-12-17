package org.nalby.yobatis.util;

public final class TextUtil {
	private TextUtil() {}
	
	public static boolean isEmpty(String text) {
		return text == null || "".equals(text.trim());
	}

}
