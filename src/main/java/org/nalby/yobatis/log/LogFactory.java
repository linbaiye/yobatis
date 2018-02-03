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
package org.nalby.yobatis.log;

import java.lang.reflect.Constructor;

public class LogFactory {
	
	private static Class<? extends Logger> loggerClass;
	
	private static final Logger nullLogger = new NullLogger();
	
	public static void setLogger(Class <? extends Logger> c) {
		loggerClass = c;
	}

	public static Logger getLogger(Class<?> clazz) {
		try {
			Constructor<?>[] constructors = loggerClass.getConstructors();
			for (Constructor<?> constructor: constructors) {
				Class<?>[] parameters = constructor.getParameterTypes();
				if (parameters.length != 1 || 
					!"String".equals(parameters[0].getSimpleName())) {
					continue;
				}
				return (Logger) constructor.newInstance(clazz.getSimpleName());
			}
		} catch (Exception e) {
			//
		}
		return nullLogger;
	}
}
