package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.nalby.yobatis.util.AntPathMatcher;
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
public class OldSpringAntPatternFileManager {
	private OldProject project;
	
	private static class FileMetadata { 
		private OldFolder folder;
		private OldPom pom;
		
		private FileMetadata(OldFolder folder, OldPom pom) {
			this.folder = folder;
			this.pom = pom;
		}

		public OldFolder getFolder() {
			return folder;
		}

		public OldPom getPom() {
			return pom;
		}
	}
	
	private OldPomTree pomTree;
	
	private final static Set<String> EMPTY_FILES = new HashSet<>(0);

	private AntPathMatcher antPathMatcher;

	private Map<String, FileMetadata> files;
	
	private Map<String, SpringXmlParser> parsers;
	
	
	private SpringXmlParser getXmlParser(String path) {
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
	}
	
	public OldSpringAntPatternFileManager(OldPomTree pomTree, OldProject project) {
		this.pomTree = pomTree;
		files = new HashMap<>();
		parsers = new HashMap<>();
		antPathMatcher = new AntPathMatcher();
		this.project = project;
	}
	
	/**
	 * Find files that match the ant path pattern in the folder.
	 * @param pom the pom module the found file belongs to.
	 * @param folder the folder
	 * @param antPattern the ant path pattern.
	 * @param result
	 */
	private void matchFilesInFolder(OldPom pom, OldFolder folder, String antPattern, Set<String> result) {
		String antpath = FolderUtil.concatPath(folder.path(), antPattern);
		for (String filepath : folder.getAllFilepaths()) {
			if (antpath.equals(filepath) || antPathMatcher.match(antpath, filepath)) {
				files.put(filepath, new FileMetadata(folder, pom));
				result.add(filepath);
			}
		}
	}
	
	private Set<String> matchFilesInResourceFolders(OldPom pom, String hint) {
		Set<String> result = new HashSet<>();
		Set<OldFolder> folders = pom.getResourceFolders();
		for (OldFolder folder : folders) {
			matchFilesInFolder(pom, folder, hint, result);
		}
		return result;
	}
	
	private Set<String> matchFilesInAllResourceFolders(String hint) {
		Set<String> result = new HashSet<>();
		for (OldPom pom : pomTree.getPoms()) {
			result.addAll(matchFilesInResourceFolders(pom, hint));
		}
		return result;
	}

	private Set<String> matchFilesInWebappFolder(String hint) {
		OldPom pom = pomTree.getWarPom();
		OldFolder folder = pom.getWebappFolder();
		Set<String> result = new HashSet<>();
		matchFilesInFolder(pom, folder, hint, result);
		return result;
	}
	
	
	private Set<String> getImportedFilesWithClasspath(String hint, OldPom pom) {
		String tokens[] = hint.split(":");
		if (hint.startsWith("classpath:")) {
			return matchFilesInResourceFolders(pom, tokens[1]);
		} 
		return matchFilesInAllResourceFolders(tokens[1]);
	}

	
	private final static String CLASSPATH_REGEX = "^classpath\\*?:.+$";
	/**
	 * Find spring's config files by hint, this should be called when
	 * searching for hints that are configured in web.xml.
	 * @param hint the hint to search.
	 * @return this files that matches this hint.
	 */
	public Set<String> findSpringFiles(String hint) {
		if (TextUtil.isEmpty(hint)) {
			return EMPTY_FILES;
		}
		OldPom webpom = pomTree.getWarPom();
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
		OldPom pom = metadata.getPom();
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
					OldPom webpom = pomTree.getWarPom();
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
		OldPom pom = metadata.getPom();
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
	 * Read properties file, and filter all placeholders if possible.
	 * @param path the properties file path.
	 * @return properties properties filtered, null if not a valid path.
	 */
	public Properties readProperties(String path) {
		if (!files.containsKey(path)) {
			return null;
		}
		OldPom pom = files.get(path).getPom();
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
	}
	
}
