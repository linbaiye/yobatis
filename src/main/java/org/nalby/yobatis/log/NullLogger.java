package org.nalby.yobatis.log;

public class NullLogger implements Logger {

	@Override
	public void info(String fomart, Object... args) {
		//Do nothing.
	}

}
