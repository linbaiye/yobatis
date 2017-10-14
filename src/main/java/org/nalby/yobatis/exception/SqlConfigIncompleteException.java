package org.nalby.yobatis.exception;

public class SqlConfigIncompleteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SqlConfigIncompleteException(String msg) {
		super(msg);
	}
	
	public SqlConfigIncompleteException(Throwable e) {
		super(e);
	}

}
