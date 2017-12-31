package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.nalby.yobatis.util.Expect;
public abstract class Project implements Folder {
	
	protected Folder root;
	
	//The full path of this project on system,
	//and will contain root.path()
	protected String syspath;
	
	public final static String MAVEN_SOURCE_CODE_PATH = "src/main/java";

	public final static String MAVEN_RESOURCES_PATH = "src/main/resources";

	public final static String WEBAPP_PATH_SUFFIX = "src/main/webapp";

	public static interface FolderSelector {
		public boolean isSelected(Folder folder);
	}
	
	public abstract String concatMavenResitoryPath(String path);
	
	public String convertToSyspath(String path) {
		Expect.notEmpty(path, "path must not be null.");
		if (path.startsWith(this.syspath)) {
			return path;
		} else if (path.startsWith(root.path())) {
			return syspath + path.replaceFirst(root.path(), "");
		} else if (!path.startsWith("/")) {
			return syspath + "/" + path;
		}
		throw new IllegalArgumentException("Not a valid path:" + path);
	}
	
	@Override
	public boolean containsFile(String filepath) {
		Expect.notEmpty(filepath, "filepath must not be null.");
		filepath = wipeRootFolderPath(filepath);
		return root.containsFile(filepath);
	}
	
	@Override
	public List<Folder> getSubfolders() {
		return this.root.getSubfolders();
	}
	
	private String wipeRootFolderPath(String path) {
		if (path.startsWith(root.path())) {
			return path.replaceFirst(root.path() + "/", "");
		}
		return path;
	}
	
	@Override
	public void writeFile(String filepath, String content) {
		Expect.notEmpty(filepath, "filepath must not be null.");
		filepath = wipeRootFolderPath(filepath);
		root.writeFile(filepath, content);
	}
	
	@Override
	public String path() {
		return root.path();
	}

	@Override
	public boolean containsFolders() {
		return root.containsFolders();
	}

	@Override
	public String name() {
		return root.name();
	}

	@Override
	public Folder createFolder(String folderPath) {
		Expect.notEmpty(folderPath, "folder path must not be empty.");
		folderPath = wipeRootFolderPath(folderPath);
		return root.createFolder(folderPath);
	}

	@Override
	public Folder findFolder(String folderpath) {
		Expect.notEmpty(folderpath, "folderpath must not be null.");
		folderpath = wipeRootFolderPath(folderpath);
		return root.findFolder(folderpath);
	}

	@Override
	public Set<String> getFilenames() {
		return root.getFilenames();
	}
	
	@Override
	public Set<Folder> getAllFolders() {
		return root.getAllFolders();
	}

	@Override
	public Set<String> getAllFilepaths() {
		return root.getAllFilepaths();
	}
	
	@Override
	public InputStream openFile(String filepath) {
		Expect.notEmpty(filepath, "folderpath must not be null.");
		filepath = wipeRootFolderPath(filepath);
		return root.openFile(filepath);
	}
}
