package org.nalby.yobatis.exception;

public class InvalidMybatisGeneratorConfigException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public InvalidMybatisGeneratorConfigException(String msg) {
		super(msg == null? "Invalid mybatis generator config exception." : msg);
	}
	
	public InvalidMybatisGeneratorConfigException(Throwable e) {
		super(e);
	}


}
