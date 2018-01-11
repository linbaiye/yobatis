package org.nalby.yobatis.structure.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.structure.AbstractFolder;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;


public final class EclipseFolder extends AbstractFolder {
	
	private IResource resource;
	
	public EclipseFolder(String parentPath, IResource resource) {
		Expect.notNull(parentPath, "parentpath must not be null.");
		Expect.notNull(resource, "wrapped folder must not be null.");
		Expect.asTrue(resource instanceof IProject || resource instanceof IFolder, "Invalid resource.");
		this.resource = resource;
		this.path = FolderUtil.concatPath(parentPath, resource.getName());
		this.name = resource.getName();
	}
	
	
	private interface ObjectCreater<T> {
		T create(IResource resource);
	}

	private IResource[] getMembers() {
		try {
			if (resource instanceof IFolder) {
				return ((IFolder)resource).members();
			}
			return ((IProject)resource).members();
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}
	
	private IFile getIFile(String name){
		try {
			if (resource instanceof IFolder) {
				return ((IFolder)resource).getFile(name);
			}
			return ((IProject)resource).getFile(name);
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}
	
	
	private IFolder getIFolder(String name){
		try {
			if (resource instanceof IFolder) {
				return ((IFolder)resource).getFolder(name);
			}
			return ((IProject)resource).getFolder(name);
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}
	

	private <T> List<T> createList(int type, ObjectCreater<T> objectCreater) {
		List<T> list = new ArrayList<>();
		for (IResource resource : getMembers()) {
			if (resource.getType() == type) {
				list.add(objectCreater.create(resource));
			}
		}
		return list;
	}
	
	
	@Override
	protected List<Folder> doListFolders() {
		return createList(IResource.FOLDER, new ObjectCreater<Folder>() {
			@Override
			public Folder create(IResource resource) {
				return new EclipseFolder(EclipseFolder.this.path, (IFolder)resource);
			}
		});
	}

	@Override
	protected List<File> doListFiles() {
		return createList(IResource.FILE, new ObjectCreater<File>() {
			@Override
			public File create(IResource resource) {
				return new EclipseFile(EclipseFolder.this.path, (IFile)resource);
			}
		});
	}


	@Override
	protected Folder doCreateFolder(String name) {
		IFolder iFolder = getIFolder(name);
		try {
			if (!iFolder.exists()) {
				iFolder.create(true, true, null);
				iFolder.refreshLocal(0, null);
			}
			return new EclipseFolder(path, iFolder);
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}


	@Override
	protected File doCreateFile(String name) {
		IFile iFile = getIFile(name);
		return EclipseFile.createFile(path, iFile);
	}

}
