package org.nalby.yobatis.structure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.exception.ResourceNotFoundException;
import org.nalby.yobatis.util.Expect;

public abstract class Project {
	
	protected Folder root;
	
	//The full path of this project on system,
	//and will contain root.path()
	protected String syspath;
	
	public final static String MAVEN_SOURCE_CODE_PATH = "src/main/java";

	protected final static String MAVEN_RESOURCES_PATH = "src/main/resources";

	protected final static String WEB_XML_PATH = "src/main/webapp/WEB-INF/web.xml";
	
	protected final static String CLASSPATH_PREFIX = "classpath:";
	
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
	
	private String convertToProjectPath(String path) {
		if (path.startsWith(root.path())
			||!path.startsWith("/")) {
			return path.replaceFirst(root.path() + "/", "");
		}
		if (path.startsWith(this.syspath)) {
			return path.replaceFirst(this.syspath + "/", "");
		}
		throw new IllegalArgumentException("Not a valid path:" + path);
	}
	
	private List<String> pathList(FolderSelector selector) {
		List<Folder> folders = findFolders(selector);
		List<String> result = new LinkedList<String>();
		for (Folder folder : folders) {
			result.add(convertToSyspath(folder.path()));
		}
		return result;
	}
	
	/**
	 * List the full path of the possible 'DAO' layers.
	 * @return possible paths if any, empty list else.
	 */
	public List<String> getSyspathsOfDao() {
		return pathList(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return folder.path().contains(MAVEN_SOURCE_CODE_PATH) &&
						("dao".equals(folder.name()) || "repository".equals(folder.name()));
			}
		});
	}
	
	/**
	 * List full paths of the possible 'model' layers.
	 * @return possible paths if any, empty list else.
	 */
	public List<String> getSyspathsOfModel() {
		return pathList(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return folder.path().contains(MAVEN_SOURCE_CODE_PATH) && 
						("model".equals(folder.name()) || "domain".equals(folder.name()));
			}
		});
	}
	
	/**
	 * List full paths of possible resources which are close to the dao layer. By 'close to', 
	 * it means the ones that are contained by the same submodule containing 'dao'.
	 * @return A list that contains path names.
	 */
	public List<String> getSyspathsOfResources() {
		List<String> paths = getSyspathsOfDao();
		List<String> result = new LinkedList<String>();
		for (String path: paths) {
			String tmp = path.replaceFirst(MAVEN_SOURCE_CODE_PATH + ".+$", MAVEN_RESOURCES_PATH);
			result.add(tmp);
		}
		return result;
	}

	
	public void closeInputStream(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				//Nothing to do.
			}
		}
	}
	

	/**
	 * Get {@code InputStream} of the {@code filepath}, if {@code filepath} represents a filename,
	 * this method tries to convert it to full path first. The caller needs to close 
	 * the inputstream, calling {@code closeInputStream} if prefer.
	 * @param filepath the file to open.
	 * @return the InputStream representing the file.
	 * @throws FileNotFoundException
	 */
	public InputStream getInputStream(String filepath) throws FileNotFoundException {
		Expect.notEmpty(filepath, "filepath must not be null.");
		return new FileInputStream(convertToSyspath(filepath));
	}


	public String readFile(String filepath) {
		Expect.notEmpty(filepath, "filepath must not be null.");
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = getInputStream(filepath);
			InputStreamReader streamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(streamReader);
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		} catch (IOException e) {
			throw new ResourceNotAvailableExeception(e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				//Ignore
			}
		}
	}
	
	/**
	 * Find folders that meet the criteria given by selector.
	 * @param selector the selector to give the criteria.
	 * @return the folders or empty list if none is met.
	 */
	public List<Folder> findFolders(FolderSelector selector) {
		Expect.notNull(selector, "selector must not be null.");
		Stack<Folder> stack = new Stack<Folder>();
		stack.push(root);
		List<Folder> result = new LinkedList<Folder>();
		do {
			Folder node = stack.pop();
			List<Folder> subFolders = node.getSubFolders();
			for (Folder child: subFolders) {
				if (selector.isSelected(child)) {
					result.add(child);
				}
				if (child.containsFolders()) {
					stack.push(child);
				}
			}
		} while (!stack.isEmpty());
		return result;
	}
	
	/**
	 * Find folders that contain the filename.
	 * @param path
	 * @return the folders that contain the filename, empty list returned if not found.
	 */
	public List<Folder> findFoldersContainingFile(final String path) {
		Expect.notEmpty(path, "path must not be empty.");
		return findFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				if (path.indexOf("/") == -1) {
					//only filename.
					return folder.containsFile(path);
				}
				//file path.
				String folderPath  = path.replaceFirst("/.*$", "");
				String filename = path.replaceFirst(folderPath + "/", "");
				return folder.path().indexOf(folderPath) != -1 && folder.containsFile(filename);
			}
		});
	}
	
	
	private String[] splitPath(String path) {
		Expect.notEmpty(path, "path must not be null.");
		path = convertToProjectPath(path);
		return path.split("/");
	}

	public boolean containsFile(String path) {
		String[] tokens = splitPath(path);
		try {
			Folder folder = root;
			for (int i = 0; i < tokens.length; i++) {
				// In case of paths like 'name1//name2'
				if ("".equals(tokens[i])) {
					continue;
				}
				if (i == tokens.length - 1) {
					return folder.containsFile(tokens[i]);
				} else {
					folder = folder.findFolder(tokens[i]);
				}
			}
		} catch (ResourceNotFoundException e) {
			// ignore.
		}
		return false;
	}

	
	public void writeFile(String path, String content) {
		Expect.notEmpty(content, "content must not be null.");
		String[] tokens = splitPath(path);
		Folder folder = root;
		for (int i = 0; i < tokens.length; i++) {
			//In case of paths like 'name1//name2'
			if ("".equals(tokens[i])) {
				continue;
			}
			if (i == tokens.length - 1) {
				folder.writeFile(tokens[i], content);
			} else {
				folder = folder.createFolder(tokens[i]);
			}
		}
	}
}
