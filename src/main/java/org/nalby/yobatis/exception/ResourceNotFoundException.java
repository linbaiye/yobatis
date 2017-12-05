package org.nalby.yobatis.exception;

public class ResourceNotFoundException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String err) {
		super(err);
	}

	public ResourceNotFoundException(Throwable throwable) {
		super(throwable);
	}

}
