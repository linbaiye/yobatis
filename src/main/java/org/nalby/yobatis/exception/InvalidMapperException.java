package org.nalby.yobatis.exception;

public class InvalidMapperException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public InvalidMapperException(String msg) {
		super(msg == null? "Invalid mybatis generator config exception." : msg);
	}
	
	public InvalidMapperException(Throwable e) {
		super(e);
	}

}
