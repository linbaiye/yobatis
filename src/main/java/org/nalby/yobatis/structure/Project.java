package org.nalby.yobatis.structure;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.nalby.yobatis.util.Expect;

public abstract class Project {
	
	protected Folder root;
	
	//The full path of this project on system.
	protected String syspath;
	
	protected final static String MAVEN_SOURCE_CODE_PATH = "src/main/java";

	protected final static String MAVEN_RESOURCES_PATH = "src/main/resources";

	protected final static String WEB_XML_PATH = "src/main/webapp/WEB-INF/web.xml";
	
	protected final static String CLASSPATH_PREFIX = "classpath:";
	
	public abstract String getDatabaseUrl();
	
	public abstract String getDatabaseUsername();
	
	public abstract String getDatabasePassword();
	
	public abstract String getDatabaseDriverClassName();
	
	public abstract String getDatabaseConnectorPath();

	public abstract String getDatabaseConnectorFullPath();
	
	public abstract String getSourceCodeDirPath();

	public abstract String getResourcesDirPath();
	
	public abstract String getModelLayerPath();

	public abstract String getDaoLayerPath();
	
	public abstract void createDir(String dirPath);
	
	public static interface FolderSelector {
		public boolean isSelected(Folder folder);
	}
	
	public abstract String concatMavenResitoryPath(String path);
	
	public boolean containsFile(String filename) {
		return root.containsFile(filename);
	}

	public  String getFullPath() {
		return syspath + root.path();
	}
	
	public String convertToFullPath(String path) {
		if (path.startsWith("/")) {
			return syspath + path;
		}
		return syspath + "/" + path;
	}
	
	private List<String> pathList(FolderSelector selector) {
		List<Folder> folders = findFolders(selector);
		List<String> result = new LinkedList<String>();
		for (Folder folder : folders) {
			result.add(convertToFullPath(folder.path()));
		}
		return result;
	}
	
	private final static Pattern PATTERN = Pattern.compile("^.+" + MAVEN_SOURCE_CODE_PATH + "/(.+)$");
	
	/**
	 * Parse package name from the {@code path}. 
	 * @param path
	 * @return package name if the path follows the maven  naming rule, null if can't find.
	 */
	public  String getPackageName(String path) {
		if (path == null || !path.contains(MAVEN_SOURCE_CODE_PATH)) {
			return null;
		}
		Matcher matcher = PATTERN.matcher(path);
		String ret = null;
		if (matcher.find()) {
			ret = matcher.group(1);
		}
		if (ret != null) {
			ret = ret.replaceAll("/", ".");
		}
		return ret;
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
	 * List full paths of possible resources which are close to the dao layer. By 'close to', it means
	 * the ones that are contained by the same submodule containing 'dao'.
	 * @return full paths
	 */
	public List<String> getSyspathsOfResources() {
		List<String> paths = getSyspathsOfDao();
		List<String> result = new LinkedList<String>();
		for (String path: paths) {
			String tmp = path.replaceFirst(MAVEN_RESOURCES_PATH + ".+$", MAVEN_RESOURCES_PATH);
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
	 * Get {@code InputStream} of the {@code filepath}, if {@code filepath} represents a filename, this method
	 * tries to convert it to full path first. The caller needs to close the inputstream, calling {@code closeInputStream} if prefer.
	 * @param filepath the file to open.
	 * @return the InputStream representing the file.
	 * @throws FileNotFoundException
	 */
	public InputStream getInputStream(String filepath) throws FileNotFoundException {
		if (!filepath.startsWith(root.path())) {
			filepath = root.path() + "/" + filepath;
		}
		if (filepath.indexOf(syspath) != -1) {
			return new FileInputStream(filepath);
		} else {
			return new FileInputStream(convertToFullPath(filepath));
		}
	}
	
	/**
	 * Find folders that meet the criteria given by selector.
	 * @param selector the selector to give the criteria.
	 * @return the folders or empty list if none is met.
	 */
	public List<Folder> findFolders(FolderSelector selector) {
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
	 * Find folders that contains the filename.
	 * @param path 
	 * @return the folders that contain the filename, empty list returned if not found.
	 */
	public List<Folder> findFoldersContainingFile(final String path) {
		return findFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				if (path.indexOf("/") == -1) {
					//only filename.
					return folder.containsFile(path);
				}
				//file path.
				String folderPath  = path.replaceFirst("/.*$", "");
				String filename = path.replace(folderPath + "/", "");
				return folder.path().indexOf(folderPath) != -1 && folder.containsFile(filename);
			}
		});
	}
	
	public void writeFile(String path, String content) {
		Expect.asTrue(path != null && content != null, "invalid param");
		if (path.startsWith("/")) {
			Expect.asTrue(path.startsWith(root.path()), "invalid path.");
		}
		path = path.replaceFirst("^" + root.path(), "");
		String[] tokens = path.split("/");
		Folder folder = root;
		for (int i = 0; i < tokens.length; i++) {
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
