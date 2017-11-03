package org.nalby.yobatis.exception;

public class UnsupportedProjectException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnsupportedProjectException(String msg) {
		super(msg == null? "Unable to parse this project." : msg);
	}
	
	public UnsupportedProjectException(Throwable e) {
		super(e);
	}

}
