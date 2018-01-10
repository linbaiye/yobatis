package org.nalby.yobatis.structure;

import java.util.Collections;
import java.util.List;

import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.TextUtil;


public abstract class AbstractFolder implements FolderV1 {
	
	/**
	 * The path of this folder, the implementer must set it.
	 */
	protected String path;

	/**
	 * The name of this folder, the implementer must set it.
	 */
	protected String name;
	
	/**
	 * The folders that this folder contains directly.
	 */
	protected List<FolderV1> folders;
	
	/**
	 * The files that this folder contains directly.
	 */
	protected List<File> files;

	
	protected abstract List<FolderV1> doListFolders();


	protected abstract List<File> doListFiles();
	
	@Override
	public String path() {
		return path;
	}

	@Override
	public String name() {
		return name;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<FolderV1> listFolders() {
		if (folders != null) {
			return folders;
		}
		folders = doListFolders();
		if (folders == null) {
			folders = Collections.EMPTY_LIST;
		}
		return folders;
	}
	
	private void validatePath(String path) {
		Expect.asTrue(!TextUtil.isEmpty(path) && !path.startsWith("/"), "A relative path is expected, but got:" + path);
	}

	@Override
	public File findFile(String filepath) {
		return null;
	}

	@Override
	public File createFile(String filepath) {
		return null;
	}

	@Override
	public FolderV1 createFolder(String folderpath) {
		return null;
	}

	@Override
	public FolderV1 findFolder(String folerpath) {
		validatePath(folerpath);
		String[] names = folerpath.split("/");
		for (FolderV1 folder : listFolders()) {
			if (folder.name().equals(names[0])) {
				if (names.length == 1) {
					return folder;
				}
				return folder.findFolder(folerpath.replaceFirst(names[0] + "/", ""));
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<File> listFiles() {
		if (files != null) {
			return files;
		}
		files = doListFiles();
		if (files == null) {
			files = Collections.EMPTY_LIST;
		}
		return files;
	}

}
	
