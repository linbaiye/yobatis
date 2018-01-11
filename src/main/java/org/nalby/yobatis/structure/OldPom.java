package org.nalby.yobatis.structure;

import java.util.Set;

public interface OldPom {
	
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
	 * Get resource folders defined in this pom.
	 * @return resource folders or empty set if not found.
	 */
	Set<OldFolder> getResourceFolders();
	
	/**
	 * Get webapp folder in this pom.
	 * @return the folder if it's a war pom and contains a webapp folder, null else.
	 */
	OldFolder getWebappFolder();
	
	/**
	 * Get source code folder in this pom, the folder that ends with "src/main/java" if the
	 * pom is not a container.
	 * @return the source code folder, or null if it does not contain.
	 */
	OldFolder getSourceCodeFolder();
	 
	/**
	 * Filter all placeholders appearing in the text.
	 * @param text the text to filter.
	 * @return filtered text, the text itself if nothing to filter.
	 */
	String filterPlaceholders(String text);

}
