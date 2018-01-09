package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Spring;

import org.mockito.asm.tree.TryCatchBlockNode;
import org.nalby.yobatis.util.AntPathMatcher;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TextUtil;
import org.nalby.yobatis.xml.SpringXmlParser;

/**
 * SpringAntPatternFileManager helps find spring xml files, properties files and
 * read hints that are configured in the &lt;import&gt; element and properties that
 * are configured in placeholder beans.
 * 
 * @author Kyle Lin
 *
 */
public class SpringAntPatternFileManager {
	private Project project;
	
	private static class FileMetadata { 
		private Folder folder;
		private Pom pom;
		
		private FileMetadata(Folder folder, Pom pom) {
			this.folder = folder;
			this.pom = pom;
		}

		public Folder getFolder() {
			return folder;
		}

		public Pom getPom() {
			return pom;
		}
	}
	
	private PomTree pomTree;
	
	private final static Set<String> EMPTY_FILES = new HashSet<>(0);

	private AntPathMatcher antPathMatcher;

	private Map<String, FileMetadata> files;
	
	
	private SpringXmlParser getXmlParser(String path) {
		try (InputStream inputStream = project.openFile(path)){
			return new SpringXmlParser(inputStream);
		} catch (Exception e) {
			return null;
		}
	}
	
	public SpringAntPatternFileManager(PomTree pomTree, Project project) {
		this.pomTree = pomTree;
		files = new HashMap<>();
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
	private void matchFilesInFolder(Pom pom, Folder folder, String antPattern, Set<String> result) {
		String antpath = FolderUtil.concatPath(folder.path(), antPattern);
		for (String filepath : folder.getAllFilepaths()) {
			if (antpath.equals(filepath) || antPathMatcher.match(antpath, filepath)) {
				files.put(filepath, new FileMetadata(folder, pom));
				result.add(filepath);
			}
		}
	}
	
	private Set<String> matchFilesInResourceFolders(Pom pom, String hint) {
		Set<String> result = new HashSet<>();
		Set<Folder> folders = pom.getResourceFolders();
		for (Folder folder : folders) {
			matchFilesInFolder(pom, folder, hint, result);
		}
		return result;
	}
	
	private Set<String> matchFilesInAllResourceFolders(String hint) {
		Set<String> result = new HashSet<>();
		for (Pom pom : pomTree.getPoms()) {
			result.addAll(matchFilesInResourceFolders(pom, hint));
		}
		return result;
	}

	private Set<String> matchFilesInWebappFolder(String hint) {
		Pom pom = pomTree.getWarPom();
		Folder folder = pom.getWebappFolder();
		Set<String> result = new HashSet<>();
		matchFilesInFolder(pom, folder, hint, result);
		return result;
	}
	
	
	private Set<String> getImportedFilesWithClasspath(String hint, Pom pom) {
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
}
