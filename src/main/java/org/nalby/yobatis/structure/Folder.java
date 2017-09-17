package org.nalby.yobatis.structure;

import java.util.List;

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
	 * Get folders contained by this folder.
	 * @return the folders if any, or an empty list if none.
	 */
	public List<Folder> folders() ;
	
	/**
	 * Test if the folder is the DAO/Repository layer.
	 * @return true if so, false if not.
	 */
	public boolean isDaoLayer();
	
	/**
	 * Test if the folder is the Model/Domain layer.
	 * @return true if so, false if not.
	 */
	public boolean isModelLayer();
	

}
