package org.nalby.yobatis.exception;

public class InvalidSqlConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidSqlConfigException(String msg) {
		super(msg);
	}
	
	public InvalidSqlConfigException(Throwable e) {
		super(e);
	}

}
