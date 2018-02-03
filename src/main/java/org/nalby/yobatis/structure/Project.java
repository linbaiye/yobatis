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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

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
	
	private static interface FileAppender<T> {
		void append(Set<T> resources, Folder folder);
	}
	
	private static <T> Set<T> iterateTree(Folder folder, FileAppender<T> fileAppender) {
		Set<T> result = new HashSet<>();
		Stack<Folder> stack = new Stack<>();
		stack.push(folder);
		while (!stack.isEmpty()) {
			Folder node = stack.pop();
			fileAppender.append(result, node);
			for (Folder tmp : node.listFolders()) {
				stack.push(tmp);
			}
		}
		return result;
	}
	
	/**
	 * List folders of all depth contained by this folder.
	 * @param folder the folder to list.
	 * @return all the folders if any, an empty set otherwise.
	 */
	public static Set<Folder> listAllFolders(Folder folder) {
		Expect.notNull(folder, "Folder must not be null.");
		return iterateTree(folder, new FileAppender<Folder>() {
			@Override
			public void append(Set<Folder> resources, Folder folder) {
				resources.addAll(folder.listFolders());
			}
		});
	}
	
	/**
	 * List files of all depth contained by this folder.
	 * @param folder the folder to list.
	 * @return all the files if any, an empty set otherwise.
	 */
	public static Set<File> listAllFiles(Folder folder) {
		Expect.notNull(folder, "Folder must not be null.");
		return iterateTree(folder, new FileAppender<File>() {
			@Override
			public void append(Set<File> resources, Folder folder) {
				resources.addAll(folder.listFiles());
			}
		});
	}

}
