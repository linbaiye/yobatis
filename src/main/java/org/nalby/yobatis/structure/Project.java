package org.nalby.yobatis.structure;

import java.util.List;

import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;

public abstract class Project implements Folder {

	/**
	 * The folder of the project itself.
	 */
	protected Folder root;
	
	/**
	 * It is left to the platform to find out where the maven repository is.
	 * @return the maven path, null if failed to find.
	 */
	abstract protected String findMavenRepositoryPath();

	private String wipeRootFolderPath(String path) {
		Expect.notEmpty(path, "path must not be empty.");
		if (path.startsWith(root.path())) {
			return path.replaceFirst(root.path() + "/", "");
		}
		return path;
	}


	public String concatMavenRepositoryPath(String path) {
		Expect.notEmpty(path, "path must not be null.");
		return FolderUtil.concatPath(findMavenRepositoryPath(), path);
	}

	@Override
	public String path() {
		return root.path();
	}

	@Override
	public String name() {
		return root.name();
	}

	@Override
	public List<Folder> listFolders() {
		return root.listFolders();
	}

	@Override
	public File findFile(String filepath) {
		return root.findFile(wipeRootFolderPath(filepath));
	}

	@Override
	public File createFile(String filepath) {
		return root.createFile(wipeRootFolderPath(filepath));
	}

	@Override
	public Folder createFolder(String folderpath) {
		return root.createFolder(wipeRootFolderPath(folderpath));
	}

	@Override
	public Folder findFolder(String folerpath) {
		return root.findFolder(wipeRootFolderPath(folerpath));
	}

	@Override
	public List<File> listFiles() {
		return root.listFiles();
	}

}
