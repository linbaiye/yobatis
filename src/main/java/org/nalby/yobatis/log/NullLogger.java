package org.nalby.yobatis.log;

public class NullLogger implements Logger {

	@Override
	public void info(String fomart, Object... args) {
		//Do nothing.
	}

	@Override
	public void error(String fomart, Object... args) {
		// TODO Auto-generated method stub
	}

}
