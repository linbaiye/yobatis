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


import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.nalby.yobatis.util.AntPathMatcher;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TextUtil;
import org.nalby.yobatis.xml.SpringXmlParser;

/**
 * SpringAntPatternFileManager helps find spring xml files, properties files and
 * read hints that are configured in the &lt;import&gt; element and properties that
 * are configured in placeholder beans.
 * 
 * <p>Must start iterating files by calling {@link #findSpringFiles(String hint) findSpringFiles} method.
 * 
 * @author Kyle Lin
 *
 */
public class SpringAntPathFileManager {

	private PomTree pomTree;
	
	private AntPathMatcher antPathMatcher;

	private Map<File, Pom> fileToPom;
	
	private Map<File, SpringXmlParser> parsers;
	
	
	public SpringAntPathFileManager(PomTree pomTree) {
		Expect.notNull(pomTree, "pomTree must not be null.");
		this.pomTree = pomTree;
		fileToPom = new HashMap<>();
		parsers = new HashMap<>();
		antPathMatcher = new AntPathMatcher();
	}
	

	private SpringXmlParser getXmlParser(File file) {
		if (parsers.containsKey(file)) {
			return parsers.get(file);
		}
		try (InputStream inputStream = file.open()){
			SpringXmlParser parser = new SpringXmlParser(inputStream);
			parsers.put(file, parser);
			return parser;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Find files that match the ant path pattern in the folder.
	 * @param pom the pom module the found file belongs to.
	 * @param folder the folder
	 * @param antPattern the ant path pattern.
	 * @param result
	 */
	private void matchFilesInFolder(Pom pom, Folder folder, String antPattern, Set<File> result) {
		String antpath = FolderUtil.concatPath(folder.path(), antPattern);
		for (File file: Project.listAllFiles(folder)) {
			if (antpath.equals(file.path()) || antPathMatcher.match(antpath, file.path())) {
				fileToPom.put(file, pom);
				result.add(file);
			}
		}
	}
	
	/**
	 * Find all files that match with the hint under the pom.
	 * @param pom 
	 * @param hint
	 * @return files matched.
	 */
	private Set<File> matchFilesInResourceFolders(Pom pom, String hint) {
		Set<File> result = new HashSet<>();
		Set<Folder> folders = pom.getResourceFolders();
		for (Folder folder : folders) {
			matchFilesInFolder(pom, folder, hint, result);
		}
		return result;
	}
	
	
	/**
	 * Find all files that match with the hint in the whole project.
	 * @param pom 
	 * @param hint
	 * @return files matched.
	 */
	private Set<File> matchFilesInAllResourceFolders(String hint) {
		Set<File> result = new HashSet<>();
		for (Pom pom : pomTree.getPoms()) {
			result.addAll(matchFilesInResourceFolders(pom, hint));
		}
		return result;
	}

	private Set<File> matchFilesInWebappFolder(String hint) {
		Pom pom = pomTree.getWarPom();
		Folder folder = pom.getWebappFolder();
		Set<File> result = new HashSet<>();
		matchFilesInFolder(pom, folder, hint, result);
		return result;
	}
	
	
	private Set<File> getImportedFilesWithClasspath(String hint, Pom pom) {
		String tokens[] = hint.split(":");
		if (hint.startsWith("classpath:")) {
			return matchFilesInResourceFolders(pom, tokens[1]);
		} 
		return matchFilesInAllResourceFolders(tokens[1]);
	}

	
	private final static String CLASSPATH_REGEX = "^classpath\\*?:.+$";
	/**
	 * Find spring's configuration files by hint, this should be called when
	 * searching for hints that are configured in web.xml.
	 * @param hint the hint to search.
	 * @return this files that matches this hint.
	 */
	@SuppressWarnings("unchecked")
	public Set<File> findSpringFiles(String hint) {
		if (TextUtil.isEmpty(hint)) {
			return Collections.EMPTY_SET;
		}
		Pom webpom = pomTree.getWarPom();
		hint = webpom.filterPlaceholders(hint);
		if (hint.matches(CLASSPATH_REGEX)) {
			return getImportedFilesWithClasspath(hint, webpom);
		} else {
			return matchFilesInWebappFolder(hint);
		}
	}
	
	
	/*
	 * There is a difference on importing relative paths between spring files and properties files.
	 */
	private interface NonclasspathHandler {
		void handle(String hint, Set<File> result);
	}
	
	/**
	 * Find imported files in the file. The isSpringXml tells which kind of files we will search,
	 * Spring xml files if true, properties files if false.
	 * @param file
	 * @param isSpringXml
	 * @param nonclasspathHandler 
	 * @return the files found.
	 */
	@SuppressWarnings("unchecked")
	private Set<File> findImportedFiles(File file, boolean isSpringXml,
			NonclasspathHandler nonclasspathHandler) {
		if (!fileToPom.containsKey(file)) {
			return Collections.EMPTY_SET;
		}
		//Not a valid spring xml file.
		SpringXmlParser parser = getXmlParser(file);
		if (parser == null) {
			return Collections.EMPTY_SET;
		}
		Pom pom = fileToPom.get(file);
		Set<String> hints = parser.getPropertiesFileLocations();
		if (isSpringXml) {
			hints = parser.getImportedLocations();
		}
		Set<File> result = new HashSet<>();
		for (String hint : hints) {
			hint = pom.filterPlaceholders(hint);
			if (hint.matches(CLASSPATH_REGEX)) {
				result.addAll(getImportedFilesWithClasspath(hint, pom));
			} else {
				nonclasspathHandler.handle(hint, result);
			}
		}
		return result;
	}
	
	/**
	 * Find all spring files configured in the file, currently files imported in xml.
	 * @param file
	 * @return other spring xml files if found.
	 */
	public Set<File> findSpringFiles(final File file) {
		return findImportedFiles(file, true, new NonclasspathHandler() {
			@Override
			public void handle(String hint, Set<File> result) {
				matchFilesInFolder(fileToPom.get(file), file.parentFolder(), hint, result);
			}
		});
	}

	
	/**
	 * Find all spring files configured in the file.
	 * @param file
	 * @return other spring xml files if found.
	 */
	public Set<File> findPropertiesFiles(File file) {
		return findImportedFiles(file, false, new NonclasspathHandler() {
			@Override
			public void handle(String hint, Set<File> result) {
				if (!hint.startsWith("/")){
					matchFilesInFolder(fileToPom.get(file), file.parentFolder(), hint, result);
				} else {
					Pom webpom = pomTree.getWarPom();
					matchFilesInFolder(webpom, webpom.getWebappFolder(), hint, result);
				}
			}
		});
	}
	
	
	/**
	 * Search for database property in the datasource bean of the spring file, and filter 
	 * out placeholders configured in the pom if possible.
	 * @param file the properties file to search.
	 * @param name the property name.
	 * @return the value which may contains placeholders, or null if not configured in this db.
	 */
	public String lookupDbProperty(File file, String name) {
		if (!fileToPom.containsKey(file)) {
			return null;
		}
		SpringXmlParser parser = getXmlParser(file);
		if (parser == null) {
			return null;
		}
		Pom pom = fileToPom.get(file);
		if ("username".equals(name)) {
			return pom.filterPlaceholders(parser.getDbUsername());
		} else if ("password".equals(name)) {
			return pom.filterPlaceholders(parser.getDbPassword());
		} else if ("driverClassName".equals(name)) {
			return pom.filterPlaceholders(parser.getDbDriverClass());
		} else if ("url".equals(name)) {
			return pom.filterPlaceholders(parser.getDbUrl());
		}
		return null;
	}
	
	
	/**
	 * Read properties file, and filter all placeholders if the are defined in pom.
	 * @param path the properties file path.
	 * @return properties properties filtered, null if not a valid file.
	 */
	public Properties readProperties(File file) {
		if (!fileToPom.containsKey(file)) {
			return null;
		}
		Pom pom = fileToPom.get(file);
		try (InputStream inputStream = file.open()) {
			Properties result = new Properties();
			Properties properties = new Properties();
			properties.load(inputStream);
			for (String key: properties.stringPropertyNames()) {
				String text = properties.getProperty(key);
				if (TextUtil.isEmpty(text)) {
					continue;
				}
				result.put(key, pom.filterPlaceholders(text));
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
}
