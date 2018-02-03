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
