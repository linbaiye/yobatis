package org.nalby.yobatis.structure.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.structure.Project;

public class EclipseProject extends Project {

	public EclipseProject(IProject project) {
		this.root = new EclipseFolder("/", project);
		open(project);
	}
	
	private void open(IProject project)  {
		try {
			if (!project.isOpen()) {
				project.open(null);
			}
		} catch (Exception e) {
			throw new UnsupportedProjectException(e);
		}
	}
	
	@Override
	protected String findMavenRepositoryPath() {
		String home = Platform.getUserLocation().getURL().getPath();
		return home.replaceFirst("/user/$", "/.m2/repository");
	}

}
