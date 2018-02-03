/*
 *    Copyright 2018 the original author or authors.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *    use this file except in compliance with the License.  You may obtain a copy
 *    of the License at
 *    
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *    License for the specific language governing permissions and limitations under
 *    the License.
 */
package org.nalby.yobatis.structure;

import java.util.Collections;
import java.util.List;

import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TextUtil;


public abstract class AbstractFolder implements Folder {
	
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
	protected List<Folder> folders;
	
	/**
	 * The files that this folder contains directly.
	 */
	protected List<File> files;


	protected abstract List<Folder> doListFolders();


	protected abstract List<File> doListFiles();
	
	/**
	 * Create a folder based on the platform, throw a ResourceNotAvailableExeception if it is unable to create.
	 * @param name the folder name.
	 * @return the folder created.
	 * @throws ResourceNotAvailableExeception if error.
	 */
	protected abstract Folder doCreateFolder(String name);

	/**
	 * Create a file based on the platform, throw a ResourceNotAvailableExeception if it is unable to create.
	 * @param name the file name.
	 * @return the file created.
	 * @throws ResourceNotAvailableExeception if error.
	 */
	protected abstract File doCreateFile(String name);
	
	
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
	public List<Folder> listFolders() {
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
		validatePath(filepath);
		Folder targetDir = this;
		String filename = FolderUtil.filename(filepath);
		if (filepath.contains("/")) {
			targetDir = findFolder(FolderUtil.folderPath(filepath));
		}
		if (targetDir != null) {
			for (File file : targetDir.listFiles()) {
				if (file.name().equals(filename)) {
					return file;
				}
			}
		}
		return null;
	}


	@Override
	public File createFile(String filepath) {
		validatePath(filepath);
		if (filepath.contains("/")) {
			String folderpath = FolderUtil.folderPath(filepath);
			Folder folder = createFolder(folderpath);
			return folder.createFile(filepath.replaceFirst(folderpath + "/", ""));
		}
		File file = findFile(filepath);
		if (file == null) {
			file = doCreateFile(filepath);
			files.add(file);
		}
		return file;
	}


	@Override
	public Folder createFolder(String folderpath) {
		validatePath(folderpath);
		String tokens[] = folderpath.split("/");
		String thisName = tokens[0];
		Folder targetFolder = findFolder(thisName);
		if (targetFolder == null) {
			targetFolder = doCreateFolder(thisName);
			folders.add(targetFolder);
		}
		if (tokens.length == 1) {
			return targetFolder;
		}
		return targetFolder.createFolder(folderpath.replaceFirst(thisName + "/", ""));
	}


	@Override
	public Folder findFolder(String folerpath) {
		validatePath(folerpath);
		String[] names = folerpath.split("/");
		for (Folder folder : listFolders()) {
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
	
