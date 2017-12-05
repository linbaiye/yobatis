package org.nalby.yobatis.exception;

public class ResourceNotAvailableExeception extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResourceNotAvailableExeception(String err) {
		super(err);
	}

	public ResourceNotAvailableExeception(Throwable throwable) {
		super(throwable);
	}

}
