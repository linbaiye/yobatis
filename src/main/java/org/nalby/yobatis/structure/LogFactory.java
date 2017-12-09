package org.nalby.yobatis.structure;

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
