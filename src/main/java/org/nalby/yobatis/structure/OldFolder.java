package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface OldFolder {
	
	/**
	 * Get the full path relative to the project.
	 * @return the full path.
	 */
	String path();
	
	/**
	 * Test if this folder contains folders.
	 * @return true if so, false if not.
	 */
	boolean containsFolders();
	
	/**
	 * Get the name of the folder.
	 * @return the name.
	 */
	String name();
	
	/**
	 * Get folders contained by this folder directly.
	 * @return the folders if any, or an empty list if none.
	 */
	List<OldFolder> getSubfolders();
	
	/**
	 * Test if this folder contains the file.
	 * @param the file path to test.
	 * @return true if so, false if not.
	 */
	boolean containsFile(String filepath);
	
	/**
	 * Write {@code content} to file {@code filepath} under this folder.
	 * @param filepath
	 * @param content content to write.
	 */
	void writeFile(String filepath, String content);
	
	/**
	 * Create a folder (recursively if necessary) under this folder based on the path.
	 * @param path the folder path.
	 * @return the folder created.
	 */
	OldFolder createFolder(String path);

	/**
	 * Find folder under this folder.
	 * @param folerPath the folder path to find.
	 * @return the folder if found, null else.
	 */
	OldFolder findFolder(String folerPath);
	
	/**
	 * Get the names of the files under this folder directly.
	 * @return the file names.
	 */
	Set<String> getFilenames();
	
	/**
	 * Get all folders contained by this folder of any depth.
	 * @return folders if any, empty set else.
	 */
	Set<OldFolder> getAllFolders();

	/**
	 * Get all files' paths under this folder.
	 * @return files' paths if found, or empty set.
	 */
	Set<String> getAllFilepaths();
	
	/**
	 * Open the file {@code relativePath}.
	 * @param filepath the file path.
	 * @return the InputStream of the file.
	 * @throws ResourceNotFoundException if the file can not be opened.
	 */
	InputStream openFile(String filepath);
	
}
