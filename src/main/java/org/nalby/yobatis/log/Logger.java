package org.nalby.yobatis.log;

public interface Logger {

	public void info(String fomart, Object ... args);

	public void error(String fomart, Object ... args);

	public void debug(String fomart, Object ... args);
}
