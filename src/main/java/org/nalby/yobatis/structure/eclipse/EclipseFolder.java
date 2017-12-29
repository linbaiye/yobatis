package org.nalby.yobatis.structure.eclipse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.exception.ResourceNotFoundException;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TextUtil;

public class EclipseFolder implements Folder {

	private IResource wrappedFolder;

	private String path;
	
	private List<Folder> subFolders;

	private Set<Folder> allSubFolders;
	
	private List<IFile> files;
	
	private Set<String> filenames;
	
	private Set<String> filepaths;

	public EclipseFolder(String parentPath, IResource wrapped) {
		Expect.notNull(parentPath, "parent path not be null.");
		Expect.notNull(wrapped, "Folder must not be null.");
		Expect.asTrue((wrapped instanceof IFolder) || (wrapped instanceof IProject),  "Invalid type.");
		wrappedFolder = wrapped;
		path = "/".equals(parentPath) ? "/" + wrapped.getName() : parentPath + "/" + wrapped.getName();
		listResources();
	}
	
	private void listResources()  {
		filenames = new HashSet<String>();
		files = new LinkedList<IFile>();
		subFolders = new LinkedList<Folder>();
		try {
			IResource[] resources = null;
			if (wrappedFolder instanceof IProject) {
				resources = ((IProject) wrappedFolder).members();
			} else {
				resources = ((IFolder) wrappedFolder).members();
			}
			for (IResource resource: resources) {
				if (resource.getType() == IResource.FILE) {
					files.add((IFile)resource);
					filenames.add(resource.getName());
				} else if (resource.getType() == IResource.FOLDER) {
					subFolders.add(new EclipseFolder(this.path, (IFolder) resource));
				}
			}
		} catch (Exception e) {
			throw new ProjectException(e);
		}
	}

	@Override
	public boolean containsFolders() {
		return !subFolders.isEmpty();
	}

	@Override
	public List<Folder> getSubFolders() {
		return subFolders;
	}

	@Override
	public boolean containsFile(String name) {
		return filenames.contains(name);
	}

	@Override
	public String path() {
		return this.path;
	}

	@Override
	public String name() {
		return wrappedFolder.getName();
	}
	
	private void open() throws CoreException {
		if (wrappedFolder instanceof IProject) {
			IProject iProject = (IProject)wrappedFolder;
			if (!iProject.isOpen()) {
				iProject.open(null);
			}
		}
	}

	@Override
	public void writeFile(String filename, String content) {
		Expect.asTrue(filename != null && filename.indexOf("/") == -1, "filename must not contain '/'.");
		try {
			IFile file = null;
			if (wrappedFolder instanceof IProject) {
				open();
				file = ((IProject)wrappedFolder).getFile(filename);
			} else {
				file = ((IFolder)wrappedFolder).getFile(filename);
			}
			file.refreshLocal(0, null);
			if (file.exists()) {
				file.delete(true, false, null);
			}
			InputStream inputStream = new ByteArrayInputStream(content.getBytes());
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
	
	@Override
	public Folder findFolder(String folderpath) {
		Expect.notEmpty(folderpath, "folderpath must not be null.");
		if (folderpath.startsWith("/")) {
			return null;
		}
		String[] names = folderpath.split("/");
		for (Folder folder : subFolders) {
			if (TextUtil.isEmpty(names[0])) {
				continue;
			}
			if (folder.name().equals(names[0])) {
				if (names.length == 1) {
					return folder;
				}
				return folder.findFolder(folderpath.replaceFirst(names[0] + "/", ""));
			}
		}
		return null;
	}

	@Override
	public Folder createFolder(String folderName) {
		Expect.asTrue(folderName != null && folderName.indexOf("/") == -1, "filename must not contain '/'.");
		try {
			open();
			IFolder newFolder = null;
			if (wrappedFolder instanceof IProject) {
				newFolder = ((IProject)wrappedFolder).getFolder(folderName);
			} else {
				newFolder = ((IFolder)wrappedFolder).getFolder(folderName);
			}
			newFolder.refreshLocal(0, null);
			if (!newFolder.exists()) {
				newFolder.create(true, true, null);
				newFolder.refreshLocal(0, null);
				return new EclipseFolder(this.path, newFolder);
			}
			return findFolder(folderName);
		} catch (CoreException e) {
			throw new ProjectException(e);
		}
	}

	@Override
	public Set<String> getFilenames() {
		return filenames;
	}
	
	@Override
	public Set<Folder> getAllFolders() {
		if (allSubFolders != null) {
			return allSubFolders;
		}
		allSubFolders = new HashSet<Folder>();
		Stack<Folder> stack = new Stack<Folder>();
		stack.push(this);
		while (!stack.isEmpty()) {
			Folder folder = stack.pop();
			List<Folder> folders = folder.getSubFolders();
			for (Folder item: folders) {
				allSubFolders.add(item);
				stack.push(item);
			}
		}
		return allSubFolders;
	}
	
	@Override
	public InputStream openInputStream(String relativeFilepath) {
		Expect.notEmpty(relativeFilepath, "relativeFilepath must not be empty.");
		if (relativeFilepath.startsWith("/")) {
			throw new ResourceNotFoundException("Unable to read file: " + relativeFilepath == null? "null": relativeFilepath);
		}
		Folder targetFolder = this;
		String filename = relativeFilepath;
		if (relativeFilepath.contains("/")) {
			String basepath = FolderUtil.folderPath(relativeFilepath);
			targetFolder = findFolder(basepath);
			filename = FolderUtil.filename(relativeFilepath);
		}
		try {
			for (IFile file : ((EclipseFolder)targetFolder).files) {
				if (file.getName().equals(filename)) {
					return file.getContents();
				}
			}
		} catch (Exception e) {
			// Ignore.
		}
		throw new ResourceNotFoundException("Unable to read file: " + relativeFilepath == null? "null": relativeFilepath);
	}

	@Override
	public Set<String> getAllFilepaths() {
		if (filepaths != null) {
			return filepaths;
		}
		filepaths = new HashSet<String>();
		for (String name: getFilenames()) {
			filepaths.add(FolderUtil.concatPath(this.path, name));
		}
		getAllFolders();
		for (Folder item: allSubFolders) {
			for (String name: item.getFilenames()) {
				filepaths.add(FolderUtil.concatPath(item.path(), name));
			}
		}
		return filepaths;
	}

}
