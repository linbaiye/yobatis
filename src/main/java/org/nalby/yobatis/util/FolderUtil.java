package org.nalby.yobatis.util;

import java.io.Closeable;

public final class FolderUtil {

	private FolderUtil() {}
	
	/**
	 * Get the folder path of the file path.
	 * aa/bb/xx/test -> aa/bb/xx
	 * @return "" if not folder path, the folder path else.
	 */
	public static String folderPath(String filepath) {
		Expect.notEmpty(filepath, "filepath must not be null.");
		if (!filepath.contains("/")) {
			return "";
		}
		return filepath.replaceFirst("(^.*)/[^/]*$", "$1");
	}
	
	/**
	 * Get the file name of the file path.
	 * aa/bb/xx/test -> test
	 * @return the file name.
	 */
	public static String filename(String filepath) {
		Expect.notEmpty(filepath, "filepath must not be null.");
		return filepath.replaceFirst("^.*/([^/]*)$", "$1");
	}
	
	
	public static String concatPath(String base, String appending) {
		Expect.notEmpty(base, "base must not be null.");
		Expect.notEmpty(appending, "appending must not be null.");
		base = base.trim();
		appending = appending.trim();
		if (appending.startsWith("/")) {
			return base + appending;
		}
		return base + "/" + appending;
	}
	
	
	public static void closeStream(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (Exception e) {
			//Nothing we can do.
		}
	}
	

}
