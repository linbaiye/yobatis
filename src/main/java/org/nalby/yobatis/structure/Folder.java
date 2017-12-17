package org.nalby.yobatis.structure;

import java.util.List;
import java.util.Set;

public interface Folder {
	
	/**
	 * Get the full path relative to the project.
	 * @return the full path.
	 */
	public String path();
	
	/**
	 * Test if this folder contains folders.
	 * @return true if so, false if not.
	 */
	public boolean containsFolders();
	
	/**
	 * Get the name of the folder.
	 * @return the name.
	 */
	public String name();
	
	/**
	 * Get folders of depth one contained by this folder.
	 * @return the folders if any, or an empty list if none.
	 */
	public  List<Folder> getSubFolders();
	
	/**
	 * Test if this folder contains the file.
	 * @param the file name to test.
	 * @return true if so, false if not.
	 */
	public boolean containsFile(String name);
	
	/**
	 * Write {@code content} to file {@code filename} under this folder.
	 * @param filename
	 * @param content content to write.
	 */
	public void writeFile(String filename, String content);
	
	/**
	 * Create folder under this folder.
	 * @param folderName the folder name.
	 * @return the folder created.
	 */
	public Folder createFolder(String folderName);

	/**
	 * Find folder under this folder.
	 * @param folderName the folder name.
	 * @return the folder found.
	 * @throws ResourceNotFoundException if not found.
	 */
	public Folder findFolder(String folderName);
	

	/**
	 * Get the names of the files under this folder directly.
	 * @return the file names.
	 */
	public Set<String> getFilenames();
	
	
	/**
	 * Get all folders contained by this folder of any depth.
	 * @return folders if any, empty set else.
	 */
	public Set<Folder> getAllFolders();

	/**
	 * Get all files' paths under this folder.
	 * @return files' paths if found, or empty set.
	 */
	public Set<String> getAllFilepaths();
	
}
