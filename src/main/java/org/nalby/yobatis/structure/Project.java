package org.nalby.yobatis.structure;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.nalby.yobatis.util.Expect;

public abstract class Project {
	
	protected Folder root;
	
	//The full path of this project on system.
	protected String syspath;
	
	protected final static String MAVEN_SOURCE_CODE_PATH = "src/main/java/";

	protected final static String MAVEN_RESOURCES_PATH = "src/main/resources/";

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

	public abstract String getFullPath();
	
	public abstract void writeFile(String path, String source);
	
	public abstract void createDir(String dirPath);
	
	public static interface FolderSelector {
		public boolean isSelected(Folder folder);
	}
	
	public boolean containsFile(String filename) {
		return root.containsFile(filename);
	}
	
	public String convertToFullPath(String path) {
		Expect.notEmpty(path, "Invalid path.");
		if (path.startsWith("/")) {
			return syspath + path;
		}
		return syspath + "/" + path;
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
	 * tries to convert it to full path first.
	 * @param filepath the file to open.
	 * @return the InputStream representing the file.
	 * @throws FileNotFoundException
	 */
	public InputStream getInputStream(String filepath) throws FileNotFoundException {
		Expect.notEmpty(filepath, "filepath must not be empty.");
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
}
