package org.nalby.yobatis.structure;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

	private Project project;
	
	private PomTree pomTree;
	
	private AntPathMatcher antPathMatcher;

	private Map<File, Pom> files;
	
	private Map<File, SpringXmlParser> parsers;
	
	/*private SpringXmlParser getXmlParser(String path) {
		if (parsers.containsKey(path)) {
			return parsers.get(path);
		}
		try (InputStream inputStream = project.openFile(path)){
			SpringXmlParser parser = new SpringXmlParser(inputStream);
			parsers.put(path, parser);
			return parser;
		} catch (Exception e) {
			return null;
		}
	}*/
	
	public SpringAntPathFileManager(PomTree pomTree, Project project) {
		Expect.notNull(pomTree, "pomTree must not be null.");
		Expect.notNull(project, "project must not be null.");
		this.pomTree = pomTree;
		this.project = project;
		files = new HashMap<>();
		parsers = new HashMap<>();
		antPathMatcher = new AntPathMatcher();
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
				files.put(file, pom);
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
	/*private interface NonclasspathHandler {
		void handle(String hint, FileMetadata fileMetadata, Set<String> result);
	}
	
	private Set<String> findImportedFiles(String path, boolean isSpringXml,
			NonclasspathHandler nonclasspathHandler) {
		if (!files.containsKey(path)) {
			return EMPTY_FILES;
		}
		SpringXmlParser parser = getXmlParser(path);
		if (parser == null) {
			return EMPTY_FILES;
		}
		FileMetadata metadata = files.get(path);
		Pom pom = metadata.getPom();
		Set<String> hints = parser.getPropertiesFileLocations();
		if (isSpringXml) {
			hints = parser.getImportedLocations();
		}

		Set<String> result = new HashSet<>();
		for (String hint : hints) {
			hint = pom.filterPlaceholders(hint);
			if (hint.matches(CLASSPATH_REGEX)) {
				result.addAll(getImportedFilesWithClasspath(hint, pom));
			} else {
				nonclasspathHandler.handle(hint, metadata, result);
			}
		}
		return result;
	}
	
	public Set<String> findImportSpringXmlFiles(String path) {
		return findImportedFiles(path, true, new NonclasspathHandler() {
			@Override
			public void handle(String hint, FileMetadata fileMetadata, Set<String> result) {
				matchFilesInFolder(fileMetadata.getPom(), fileMetadata.getFolder(), hint, result);
			}
		});
	}

	
	public Set<String> findPropertiesFiles(String path) {
		return findImportedFiles(path, false, new NonclasspathHandler() {
			@Override
			public void handle(String hint, FileMetadata fileMetadata, Set<String> result) {
				if (!hint.startsWith("/")){
					matchFilesInFolder(fileMetadata.getPom(), fileMetadata.getFolder(), hint, result);
				} else {
					Pom webpom = pomTree.getWarPom();
					matchFilesInFolder(webpom, webpom.getWebappFolder(), hint, result);
				}
			}
		});
	}
	
	public String lookupPropertyOfSpringFile(String path, String name) {
		if (!files.containsKey(path)) {
			return null;
		}
		SpringXmlParser parser = getXmlParser(path);
		if (parser == null) {
			return null;
		}
		FileMetadata metadata = files.get(path);
		Pom pom = metadata.getPom();
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
	}*/
	
	
	/**
	 * Read properties file, and filter all placeholders if possible.
	 * @param path the properties file path.
	 * @return properties properties filtered, null if not a valid path.
	 */
	/*public Properties readProperties(String path) {
		if (!files.containsKey(path)) {
			return null;
		}
		Pom pom = files.get(path).getPom();
		try (InputStream inputStream = project.openFile(path)) {
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
	}*/
	
}
