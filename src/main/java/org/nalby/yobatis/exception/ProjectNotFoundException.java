package org.nalby.yobatis.exception;

public class ProjectNotFoundException extends ProjectException {
	private static final long serialVersionUID = 1L;

	public ProjectNotFoundException() {
		super("Project not found.");
	}

}
