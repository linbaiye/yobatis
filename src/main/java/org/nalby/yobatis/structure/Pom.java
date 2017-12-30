package org.nalby.yobatis.structure;

import java.util.Set;

public interface Pom {
	
	/**
	 * If this pom's &lt;packaging&gt; is a war.
	 * @return true if so, false else.
	 */
	boolean isWar();
	
	/**
	 * If this pom acts as a container.
	 * @return true if so, false else.
	 */
	boolean isContainer();
	
	/**
	 * Get the folder that contains this pom.
	 * @return the folder always.
	 */
	Folder getFolder();
	
	/**
	 * Get resource folders defined in this pom.
	 * @return resource folders or empty set if not found.
	 */
	Set<Folder> getResourceFolders();
	
	/**
	 * Get webapp folder in this pom.
	 * @return the folder if it's a war pom and contains the webapp dir indeed, null else.
	 */
	Folder getWebappFolder();
	
	/**
	 * Get source code folder in this pom, the folder that ends with "src/main/java" if the
	 * pom is not a container.
	 * @return the source code folder.
	 */
	Folder getSourceCodeFolder();
	 
	
	/**
	 * Filter all placeholders appearing in text.
	 * @param text the text to filter.
	 * @return filtered text.
	 */
	String filterPlaceholders(String text);

}
