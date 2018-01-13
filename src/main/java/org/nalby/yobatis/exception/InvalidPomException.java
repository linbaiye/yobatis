package org.nalby.yobatis.exception;

public class InvalidPomException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidPomException(String msg) {
		super(msg == null? "Invalid configuration exception." : msg);
	}
	
	public InvalidPomException(Throwable e) {
		super(e);
	}


}
