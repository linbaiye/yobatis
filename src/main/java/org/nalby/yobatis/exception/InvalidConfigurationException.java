package org.nalby.yobatis.exception;

public class InvalidConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidConfigurationException(String msg) {
		super(msg == null? "Invalid configuration exception." : msg);
	}
	
	public InvalidConfigurationException(Throwable e) {
		super(e);
	}


}
