package org.nalby.yobatis.structure;

import java.util.List;

public interface FolderV1 {
	
	/**
	 * Get the path of the folder.
	 * @return
	 */
	String path();
	
	/**
	 * Get the name of the folder.
	 * @return the name.
	 */
	String name();
	
	/**
	 * List folders contained by this folder directly.
	 * @return the folders if any, or an empty list else.
	 */
	List<FolderV1> listFolders();
	
	/**
	 * Find file by path.
	 * @param filepath the file path.
	 * @return the File if exists, null else.
	 */
	File findFile(String filepath);
	
	/**
	 * Create a {@link File} of the path, all missing folder nodes will be 
	 * created, or the file is returned if already existed.
	 * @param filepath the file path.
	 * @return the file created or returned.
	 */
	File createFile(String filepath);
	
	/**
	 * Create a folder recursively under this folder based on the path.
	 * @param path the folder path.
	 * @return the folder created.
	 */
	FolderV1 createFolder(String folderpath);

	/**
	 * Find folder under this folder.
	 * @param folerPath the folder path to find.
	 * @return the folder if found, null else.
	 */
	FolderV1 findFolder(String folerpath);

	/**
	 * List files contained by this folder directly.
	 * @return the files if any, or an empty list else.
	 */
	List<File> listFiles();
}
