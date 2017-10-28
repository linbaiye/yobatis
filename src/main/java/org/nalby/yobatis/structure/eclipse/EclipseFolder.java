package org.nalby.yobatis.structure.eclipse;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;

public final class EclipseFolder implements Folder {

	private IFolder wrappedFolder;

	private String path = null;

	private List<Folder> subFolders = null;

	public EclipseFolder(EclipseFolder parent, IFolder wrapped) {
		Expect.notNull(wrapped, "Folder must not be null.");
		wrappedFolder = wrapped;
		if (parent == null) {
			path = "";
		} else {
			path = parent.path + ("".equals(parent.path) ? "" : ".") + wrapped.getName();
		}
	}

	private void listSubFolders() throws CoreException {
		subFolders = new LinkedList<Folder>();
		IResource[] resources = wrappedFolder.members();
		for (IResource resource : resources) {
			if (resource.getType() == IResource.FOLDER) {
				subFolders.add(new EclipseFolder(this, (IFolder) resource));
			}
		}
	}

	@Override
	public String path() {
		return wrappedFolder.getLocationURI().getPath();
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
	public String name() {
		return wrappedFolder.getName();
	}

	@Override
	public List<Folder> folders() {
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
	public boolean isDaoLayer() {
		return path != null &&
			(path.endsWith("dao") || path.endsWith("repository"));
	}

	@Override
	public boolean isModelLayer() {
		return path != null &&
			(path.endsWith("model") || path.endsWith("domain"));
	}

}
