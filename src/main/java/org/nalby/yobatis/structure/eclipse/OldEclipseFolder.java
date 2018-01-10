package org.nalby.yobatis.structure.eclipse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
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
import org.nalby.yobatis.structure.OldFolder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TextUtil;

public class OldEclipseFolder implements OldFolder {

	private IResource wrappedFolder;

	private String path;
	
	/**
	 * Subfolders this folder contains directly.
	 */
	private List<OldFolder> subfolders;

	/**
	 * Subfolders this folder contains.
	 */
	private Set<OldFolder> allSubfolders;
	
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

	public OldEclipseFolder(String parentPath, IResource wrapped) {
		Expect.notNull(parentPath, "parent path not be null.");
		Expect.notNull(wrapped, "Folder must not be null.");
		Expect.asTrue((wrapped instanceof IFolder) || (wrapped instanceof IProject),  "Invalid type.");
		wrappedFolder = wrapped;
		path = "/".equals(parentPath) ? "/" + wrapped.getName() : parentPath + "/" + wrapped.getName();
		try {
			open();
		} catch (CoreException e) {
			throw new ProjectException("Failed to open project.");
		}
		listResources();
	}
	
	private void listResources()  {
		filenames = new HashSet<String>();
		files = new LinkedList<IFile>();
		subfolders = new LinkedList<OldFolder>();
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
					subfolders.add(new OldEclipseFolder(this.path, (IFolder) resource));
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
	public List<OldFolder> getSubfolders() {
		return subfolders;
	}

	@Override
	public boolean containsFile(String filepath) {
		validatePath(filepath);
		if (!filepath.contains("/")) {
			return filenames.contains(filepath);
		} else {
			OldFolder folder = findFolder(FolderUtil.folderPath(filepath));
			if (folder != null) {
				String name = FolderUtil.filename(filepath);
				return folder.containsFile(name);
			}
			return false;
		}
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
				file = ((IProject)wrappedFolder).getFile(filename);
			} else {
				file = ((IFolder)wrappedFolder).getFile(filename);
			}
			file.refreshLocal(0, null);
			if (file.exists()) {
				file.delete(true, false, null);
				Iterator<IFile> iterator = files.iterator();
				while (iterator.hasNext()) {
					IFile iFile = iterator.next();
					if (filename.equals(iFile.getName())) {
						iterator.remove();
					}
				}
			}
			try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
				file.create(inputStream, IResource.NONE, null);
				file.refreshLocal(0, null);
			}
			filenames.add(filename);
			files.add(file);
		} catch (Exception e) {
			throw new ProjectException(e);
		}
	}
	
	private void validatePath(String path) {
		Expect.asTrue(!TextUtil.isEmpty(path) && !path.startsWith("/"), "A relative path is expected, but got:" + path);
	}

	@Override
	public void writeFile(String filepath, String content) {
		validatePath(filepath);

		if (!filepath.contains("/")) {
			doWriteFile(filepath, content);
		} else {
			OldFolder folder = createFolder(FolderUtil.folderPath(filepath));
			folder.writeFile(FolderUtil.filename(filepath), content);
		}
	}

	@Override
	public OldFolder findFolder(String folderpath) {
		validatePath(folderpath);
		String[] names = folderpath.split("/");
		for (OldFolder folder : subfolders) {
			if (folder.name().equals(names[0])) {
				if (names.length == 1) {
					return folder;
				}
				return folder.findFolder(folderpath.replaceFirst(names[0] + "/", ""));
			}
		}
		return null;
	}
	
	private OldFolder doCreateFolder(String name) {
		try {
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
			OldFolder folder = new OldEclipseFolder(this.path, newFolder);
			subfolders.add(folder);
			allSubfolders = null;
			filepaths = null;
			return folder;
		} catch (CoreException e) {
			throw new ProjectException(e);
		}
	}

	@Override
	public OldFolder createFolder(String folderpath) {
		validatePath(folderpath);
		String tokens[] = folderpath.split("/");
		String thisName = tokens[0];
		OldFolder targetFolder = null;
		for (OldFolder folder : subfolders) {
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
		return targetFolder.createFolder(folderpath.replaceFirst(thisName + "/", ""));
	}

	@Override
	public Set<String> getFilenames() {
		return filenames;
	}
	
	@Override
	public Set<OldFolder> getAllFolders() {
		if (allSubfolders != null) {
			return allSubfolders;
		}
		allSubfolders = new HashSet<OldFolder>();
		Stack<OldFolder> stack = new Stack<OldFolder>();
		stack.push(this);
		while (!stack.isEmpty()) {
			OldFolder folder = stack.pop();
			List<OldFolder> folders = folder.getSubfolders();
			for (OldFolder item: folders) {
				allSubfolders.add(item);
				stack.push(item);
			}
		}
		return allSubfolders;
	}
	
	@Override
	public InputStream openFile(String filepath) {
		validatePath(filepath);
		try {
			OldFolder targetFolder = this;
			String filename = filepath;
			if (filepath.contains("/")) {
				String basepath = FolderUtil.folderPath(filepath);
				targetFolder = findFolder(basepath);
				filename = FolderUtil.filename(filepath);
			}
			for (IFile file : ((OldEclipseFolder) targetFolder).files) {
				if (file.getName().equals(filename)) {
					return file.getContents();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		for (OldFolder item: allSubfolders) {
			for (String name: item.getFilenames()) {
				filepaths.add(FolderUtil.concatPath(item.path(), name));
			}
		}
		return filepaths;
	}

}
