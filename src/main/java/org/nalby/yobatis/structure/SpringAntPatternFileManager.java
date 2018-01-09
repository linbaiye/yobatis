package org.nalby.yobatis.structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nalby.yobatis.util.AntPathMatcher;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TextUtil;

/**
 * SpringAntPatternFileManager supports finding spring xml files, properties files and
 * reading hints that are configured in the &lg;import&bg; element and properties that
 * are configured in placeholder bean.
 * 
 * @author Kyle Lin
 *
 */
public class SpringAntPatternFileManager {
	
	private static class FileMetadata { 
		private String filepath;
		private Folder folder;
		private Pom pom;
		private FileMetadata(String path, Folder folder, Pom pom) {
			this.filepath = path;
			this.folder = folder;
			this.pom = pom;
		}
		public String getFilepath() {
			return filepath;
		}
		public void setFilepath(String filepath) {
			this.filepath = filepath;
		}
		public Folder getFolder() {
			return folder;
		}
	}
	
	private PomTree pomTree;
	
	private final static Set<String> EMPTY_FILES = new HashSet<>(0);

	private AntPathMatcher antPathMatcher;


	private Map<String, FileMetadata> files;

	private Project project;
	
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
				files.put(filepath, new FileMetadata(filepath, folder, pom));
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
	
	
	private final static String CLASSPATH_REGEX = "^classpath\\*?:.*$";
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
		if (hint.startsWith("classpath:") && hint.matches(CLASSPATH_REGEX)) {
			String tokens[] = hint.split(":");
			return matchFilesInResourceFolders(webpom, tokens[1]);
		} else if (hint.startsWith("classpath*:") && hint.matches(CLASSPATH_REGEX)) {
			String tokens[] = hint.split(":");
			return matchFilesInAllResourceFolders(tokens[1]);
		} else {
			return matchFilesInWebappFolder(hint);
		}
	}

}
