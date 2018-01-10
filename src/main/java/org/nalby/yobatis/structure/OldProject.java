package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import org.nalby.yobatis.util.Expect;
public abstract class OldProject implements OldFolder {
	
	protected OldFolder root;
	

	public abstract String concatMavenResitoryPath(String path);
	
	@Override
	public boolean containsFile(String filepath) {
		Expect.notEmpty(filepath, "filepath must not be null.");
		filepath = wipeRootFolderPath(filepath);
		return root.containsFile(filepath);
	}
	
	@Override
	public List<OldFolder> getSubfolders() {
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
	public OldFolder createFolder(String folderPath) {
		Expect.notEmpty(folderPath, "folder path must not be empty.");
		folderPath = wipeRootFolderPath(folderPath);
		return root.createFolder(folderPath);
	}

	@Override
	public OldFolder findFolder(String folderpath) {
		Expect.notEmpty(folderpath, "folderpath must not be null.");
		folderpath = wipeRootFolderPath(folderpath);
		return root.findFolder(folderpath);
	}

	@Override
	public Set<String> getFilenames() {
		return root.getFilenames();
	}
	
	@Override
	public Set<OldFolder> getAllFolders() {
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
