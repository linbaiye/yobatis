package org.nalby.yobatis.structure.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.util.Expect;

public class EclipseProject extends Project {

	private IProject wrappedProject;

	public EclipseProject(IProject project) {
		this.wrappedProject = project;
		this.root = new OldEclipseFolder("/",  wrappedProject);
	}
	
	public String concatMavenResitoryPath(String path) {
		Expect.notEmpty(path, "Path should not be null.");
		String home = Platform.getUserLocation().getURL().getPath();
		// Not sure why the '/user' suffix is attached.
		if (home.endsWith("/user/")) {
			home = home.replaceFirst("/user/$", "/.m2/repository");
		}
		return home + (path.startsWith("/")? path : "/" + path);
	}
}
