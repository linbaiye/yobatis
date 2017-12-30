package org.nalby.yobatis.structure;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.nalby.yobatis.exception.ResourceNotFoundException;
import org.nalby.yobatis.util.Expect;
public abstract class Project implements Folder {
	
	protected Folder root;
	
	//The full path of this project on system,
	//and will contain root.path()
	protected String syspath;
	
	public final static String MAVEN_SOURCE_CODE_PATH = "src/main/java";

	public final static String MAVEN_RESOURCES_PATH = "src/main/resources";

	public final static String WEBAPP_PATH_SUFFIX = "src/main/webapp";

	public final static String WEB_XML_PATH = "src/main/webapp/WEB-INF/web.xml";

	protected final static String CLASSPATH_PREFIX = "classpath:";
	
	
	private Logger logger = LogFactory.getLogger(getClass());
	
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
				("dao".equals(folder.name()) ||
				"repository".equals(folder.name()) ||
				"mapper".equals(folder.name()));
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
				("entity".equals(folder.name()) || "model".equals(folder.name()) || "domain".equals(folder.name()));
			}
		});
	}
	
	/**
	 * List full paths of possible resources which are close to the dao layer. By 'close to', 
	 * it means the ones that are contained by the same submodule containing 'dao'.
	 * @return A list that contains path names.
	 */
	public Set<String> getSyspathsOfResources() {
		List<String> paths = getSyspathsOfDao();
		Set<String> result = new HashSet<String>();
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
	
	private String[] splitPath(String path) {
		Expect.notEmpty(path, "path must not be null.");
		path = convertToProjectPath(path);
		return path.split("/");
	}

	@Override
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
					if (folder == null) {
						return false;
					}
				}
			}
		} catch (ResourceNotFoundException e) {
			// ignore.
		}
		return false;
	}
	
	@Override
	public List<Folder> getSubFolders() {
		return this.root.getSubFolders();
	}
	
	@Override
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
	public Folder createFolder(String folderName) {
		return root.createFolder(folderName);
	}

	@Override
	public Folder findFolder(String folderpath) {
		Expect.notEmpty(folderpath, "folderpath must not be null.");
		if (folderpath.startsWith(root.path())) {
			folderpath = folderpath.replaceFirst(root.path(), "");
		}
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
	public InputStream openInputStream(String name) {
		return root.openInputStream(name);
	}
}
