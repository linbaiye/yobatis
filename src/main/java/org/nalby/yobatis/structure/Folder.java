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

import java.util.List;

public interface Folder {
	
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
	List<Folder> listFolders();
	
	/**
	 * Find file by path.
	 * @param filepath the file path.
	 * @return the File if exists, null else.
	 */
	File findFile(String filepath);
	
	/**
	 * Create a {@link File} of the path, all missing folder nodes will be 
	 * created, the file will be truncated if already existed.
	 * @param filepath the file path.
	 * @return the file created.
	 * @throws ResourceNotAvalaibleException if any missing folder was unable to create, 
	 * 	or could not create the file.
	 */
	File createFile(String filepath);
	
	/**
	 * Create a folder recursively under this folder based on the path.
	 * @param path the folder path.
	 * @return the folder created.
	 * @throws ResourceNotAvalaibleException if any missing folder was unable to create.
	 */
	Folder createFolder(String folderpath);

	/**
	 * Find folder under this folder.
	 * @param folerPath the folder path to find.
	 * @return the folder if found, null else.
	 */
	Folder findFolder(String folerpath);

	/**
	 * List files contained by this folder directly.
	 * @return the files if any, or an empty list else.
	 */
	List<File> listFiles();
}
