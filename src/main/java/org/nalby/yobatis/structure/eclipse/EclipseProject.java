package org.nalby.yobatis.structure.eclipse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.util.Expect;

public class EclipseProject extends Project {

	private IProject wrappedProject;

	public EclipseProject(IProject project) {
		this.wrappedProject = project;
		this.root = new EclipseFolder("/",  wrappedProject);
		this.syspath = project.getLocationURI().getPath();
	}

	@Override
	public void writeFile(String path, String source) {
		try {
			if (!wrappedProject.isOpen()) {
				wrappedProject.open(null);
			}
			IFile file = wrappedProject.getFile(path);
			file.refreshLocal(0, null);
			if (file.exists()) {
				file.delete(true, false, null);
			}
			InputStream inputStream = new ByteArrayInputStream(source.getBytes());
			file.create(inputStream, IResource.NONE, null);
			file.refreshLocal(0, null);
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new ProjectException(e);
			}
		} catch (CoreException e) {
			throw new ProjectException(e);
		}
	}
	
	public  String concatMavenResitoryPath(String path) {
		Expect.notEmpty(path, "Path should not be null.");
		String home = Platform.getUserLocation().getURL().getPath();
		// Not sure why the '/user' suffix is attached.
		if (home.endsWith("/user/")) {
			home = home.replaceFirst("/user/$", "/.m2/repository");
		}
		return home + (path.startsWith("/")? path : "/" + path);
	}

	@Override
	public void createDir(String dirPath) {
		try {
			if (!wrappedProject.isOpen()) {
				wrappedProject.open(null);
			}
			IFolder folder = wrappedProject.getFolder(dirPath);
			folder.refreshLocal(0, null);
			if (!folder.exists()) {
				folder.create(true, true, null);
				folder.refreshLocal(0, null);
			}
		} catch (CoreException e) {
			throw new ProjectException(e);
		}
	}
}
