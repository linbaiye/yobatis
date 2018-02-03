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
	 * Get resource folders defined in this pom.
	 * @return resource folders or empty set if not found.
	 */
	Set<Folder> getResourceFolders();
	
	/**
	 * Get webapp folder in this pom.
	 * @return the folder if it's a war pom and contains a webapp folder, null else.
	 */
	Folder getWebappFolder();
	
	/**
	 * Get source code folder in this pom, the folder that ends with "src/main/java" if the
	 * pom is not a container.
	 * @return the source code folder, or null if it does not contain.
	 */
	Folder getSourceCodeFolder();
	 
	/**
	 * Filter all placeholders appearing in the text.
	 * @param text the text to filter.
	 * @return filtered text, the text itself if nothing to filter.
	 */
	String filterPlaceholders(String text);

}
