package org.nalby.yobatis.structure.eclipse;

import java.io.ByteArrayInputStream;
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
	
	/**
	 * Subfolders this folder contains directly.
	 */
	private List<Folder> subfolders;

	/**
	 * Subfolders this folder contains.
	 */
	private Set<Folder> allSubfolders;
	
	private List<IFile> files;
	
	/**
	 * Names of the files contained by this folder directly.
	 */
	private Set<String> filenames;
	
	/**
	 * Paths of the files contained by this folder, should be absolute paths if contained
	 * by the project folder, relative paths else.
	 */
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
		subfolders = new LinkedList<Folder>();
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
					subfolders.add(new EclipseFolder(this.path, (IFolder) resource));
				}
			}
		} catch (Exception e) {
			throw new ProjectException(e);
		}
	}

	@Override
	public boolean containsFolders() {
		return !subfolders.isEmpty();
	}

	@Override
	public List<Folder> getSubfolders() {
		return subfolders;
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
	
	private void doWriteFile(String filename, String content) {
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
			try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
				file.create(inputStream, IResource.NONE, null);
				file.refreshLocal(0, null);
			}
		} catch (Exception e) {
			throw new ProjectException(e);
		}
	}

	@Override
	public void writeFile(String filepath, String content) {
		Expect.asTrue(!TextUtil.isEmpty(filepath) && !filepath.startsWith("/"), "A relative path is expected.");
		if (!filepath.contains("/")) {
			doWriteFile(filepath, content);
		} else {
			Folder folder = createFolder(FolderUtil.folderPath(filepath));
			folder.writeFile(FolderUtil.filename(filepath), content);
		}
	}

	@Override
	public Folder findFolder(String folderpath) {
		Expect.asTrue(!TextUtil.isEmpty(folderpath) && !folderpath.startsWith("/"), "A relative path is expected.");
		String[] names = folderpath.split("/");
		for (Folder folder : subfolders) {
			if (folder.name().equals(names[0])) {
				if (names.length == 1) {
					return folder;
				}
				return folder.findFolder(folderpath.replaceFirst(names[0] + "/", ""));
			}
		}
		return null;
	}
	
	private Folder doCreateFolder(String name) {
		try {
			open();
			IFolder newFolder = null;
			if (wrappedFolder instanceof IProject) {
				newFolder = ((IProject) wrappedFolder).getFolder(name);
			} else {
				newFolder = ((IFolder) wrappedFolder).getFolder(name);
			}
			if (!newFolder.exists()) {
				newFolder.create(true, true, null);
				newFolder.refreshLocal(0, null);
			}
			Folder folder = new EclipseFolder(this.path, newFolder);
			subfolders.add(folder);
			return folder;
		} catch (CoreException e) {
			throw new ProjectException(e);
		}
	}

	@Override
	public Folder createFolder(String path) {
		Expect.asTrue(!TextUtil.isEmpty(path) && !path.startsWith("/"), "A relative path is expected.");
		String tokens[] = path.split("/");
		String thisName = tokens[0];
		Folder targetFolder = null;
		for (Folder folder : subfolders) {
			if (folder.name().equals(thisName)) {
				targetFolder = folder;
				break;
			}
		}
		if (targetFolder == null) {
			targetFolder = doCreateFolder(thisName);
		}
		if (tokens.length == 1) {
			return targetFolder;
		}
		return targetFolder.createFolder(path.replace(thisName + "/", ""));
	}

	@Override
	public Set<String> getFilenames() {
		return filenames;
	}
	
	@Override
	public Set<Folder> getAllFolders() {
		if (allSubfolders != null) {
			return allSubfolders;
		}
		allSubfolders = new HashSet<Folder>();
		Stack<Folder> stack = new Stack<Folder>();
		stack.push(this);
		while (!stack.isEmpty()) {
			Folder folder = stack.pop();
			List<Folder> folders = folder.getSubfolders();
			for (Folder item: folders) {
				allSubfolders.add(item);
				stack.push(item);
			}
		}
		return allSubfolders;
	}
	
	@Override
	public InputStream openFile(String filepath) {
		Expect.asTrue(!TextUtil.isEmpty(filepath) && !filepath.startsWith("/"), "A relative path is expected.");
		Folder targetFolder = this;
		String filename = filepath;
		if (filepath.contains("/")) {
			String basepath = FolderUtil.folderPath(filepath);
			targetFolder = findFolder(basepath);
			filename = FolderUtil.filename(filepath);
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
		throw new ResourceNotFoundException("Unable to read file: " + filepath);
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
		for (Folder item: allSubfolders) {
			for (String name: item.getFilenames()) {
				filepaths.add(FolderUtil.concatPath(item.path(), name));
			}
		}
		return filepaths;
	}

}
