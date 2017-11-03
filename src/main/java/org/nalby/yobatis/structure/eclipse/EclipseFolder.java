package org.nalby.yobatis.structure.eclipse;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;

public  class EclipseFolder implements Folder {

	private IResource wrappedFolder;

	private String path;
	
	private List<Folder> subFolders;

	public EclipseFolder(String parentPath, IResource wrapped) {
		Expect.notNull(parentPath, "parent path not be null.");
		Expect.notNull(wrapped, "Folder must not be null.");
		Expect.asTrue((wrapped instanceof IFolder) || (wrapped instanceof IProject),  "Invalid type.");
		wrappedFolder = wrapped;
		path = "/".equals(parentPath)? "/" : parentPath + "/" + wrapped.getName();
	}

	private void listSubFolders() throws CoreException {
		subFolders = new LinkedList<Folder>();
		IResource[] resources = null;
		if (wrappedFolder instanceof IProject) {
			resources = ((IProject)wrappedFolder).members();
		} else {
			resources = ((IFolder)wrappedFolder).members();
		}
		for (IResource resource : resources) {
			if (resource.getType() == IResource.FOLDER) {
				if ("target".equals(resource.getName()) && this.containsFile("pom.xml")) {
					continue;
				}
				subFolders.add(new EclipseFolder(this.path, (IFolder) resource));
			}
		}
	}

	@Override
	public boolean containsFolders() {
		try {
			if (subFolders == null) {
				listSubFolders();
			}
			return !subFolders.isEmpty();
		} catch (CoreException e) {
			return false;
		}
	}

	@Override
	public List<Folder> getSubFolders() {
		try {
			if (subFolders == null) {
				listSubFolders();
			}
		} catch (CoreException e) {
			//Do nothing since the subFolders will be empty.
		}
		return subFolders;
	}

	@Override
	public boolean containsFile(String name) {
		if (name == null || "".equals(name.trim())) {
			return false;
		}
		IFile file = null;
		if (wrappedFolder instanceof IProject) {
			file = ((IProject)wrappedFolder).getFile(name);
		} else {
			file = ((IFolder)wrappedFolder).getFile(name);
		}
		return file.exists();
	}

	@Override
	public String path() {
		return this.path;
	}

	@Override
	public String name() {
		return wrappedFolder.getName();
	}
}
