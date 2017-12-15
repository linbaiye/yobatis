package org.nalby.yobatis.util;

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
	 * Get the folder path of the file path.
	 * aa/bb/xx/test -> test
	 * @return the file name.
	 */
	public static String filename(String filepath) {
		Expect.notEmpty(filepath, "filepath must not be null.");
		return filepath.replaceFirst("^.*/([^/]*)$", "$1");
	}
	
	
	public static String concatPath(String base, String appending) {
		Expect.notEmpty(base, "base must not be null.");
		Expect.notEmpty(base, "appending must not be null.");
		if (appending.startsWith("/")) {
			return base + appending;
		}
		return base + "/" + appending;
	}

}
