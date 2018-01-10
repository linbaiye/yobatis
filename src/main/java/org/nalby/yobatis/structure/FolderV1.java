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
	 * Write {@code content} to the file of the path, and return the file. If the file
	 * is already existed, it will be overwrote.
	 * @param filepath the file path.
	 * @param content content to write.
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
