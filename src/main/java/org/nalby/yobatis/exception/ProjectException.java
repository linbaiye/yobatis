package org.nalby.yobatis.exception;

public class ProjectException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ProjectException(String msg) {
		super(msg == null? "General project exception." : msg);
	}
	
	public ProjectException(Throwable e) {
		super(e);
	}

}
