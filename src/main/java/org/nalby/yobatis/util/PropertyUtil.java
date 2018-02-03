/*
 *    Copyright 2018 the original author or authors.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *    use this file except in compliance with the License.  You may obtain a copy
 *    of the License at
 *    
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *    License for the specific language governing permissions and limitations under
 *    the License.
 */
package org.nalby.yobatis.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PropertyUtil {

	private PropertyUtil() {}
	
	private static final String PLACEHOLDER_PATTERN_STRING = "^\\s*\\$\\{([^\\{\\}]*)\\}\\s*$";

	private static final Pattern PLACEHOLDER_SUBPATTERN = Pattern.compile("(\\$\\{[^\\{\\}]*\\})");

	public static boolean isPlaceholder(String str) {
		if (str == null) {
			return false;
		}
		return Pattern.matches(PLACEHOLDER_PATTERN_STRING, str);
	}
	
	public static String valueOfPlaceholder(String str) {
		if (!isPlaceholder(str)) {
			return str;
		}
		String tmp = str.replaceFirst(PLACEHOLDER_PATTERN_STRING, "$1");
		return tmp.trim();
	}
	
	/**
	 * Test if the str contains any placeholder.
	 * @param str the string to test
	 * @return true if so, false else.
	 */
	public static boolean containsPlaceholder(String str) {
		if (TextUtil.isEmpty(str)) {
			return false;
		}
		Matcher matcher = PLACEHOLDER_SUBPATTERN.matcher(str);
		return matcher.find();
	}
	

	private final static List<String> EMPTY_LIST = new ArrayList<String>(0);
	public static List<String> placeholdersFrom(String str) {
		if (!containsPlaceholder(str)) {
			return EMPTY_LIST;
		}
		List<String> set = new LinkedList<String>();
		Matcher matcher = PLACEHOLDER_SUBPATTERN.matcher(str);
		while(matcher.find()) {
			set.add(matcher.group(1));
		}
		return set;
	}
}
