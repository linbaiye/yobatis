package org.nalby.yobatis.structure;

import java.util.List;

import org.nalby.yobatis.util.Expect;

public abstract class Project implements Folder {

	/**
	 * The folder of the project itself.
	 */
	protected Folder root;
	
	public abstract String concatMavenResitoryPath(String path);

	private String wipeRootFolderPath(String path) {
		Expect.notEmpty(path, "path must not be empty.");
		if (path.startsWith(root.path())) {
			return path.replaceFirst(root.path() + "/", "");
		}
		return path;
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
