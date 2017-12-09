package org.nalby.yobatis.structure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.xml.SpringXmlParser;
/**
 * Used to parse spring configuration files. The main purpose is to locate
 * database's properties.
 * 
 * @author Kyle Lin
 */
public class SpringParser {

	private Project project;

	private List<SpringXmlParser> springXmlParsers;

	/**
	 * Parse spring configuration according to the init-params configured
	 * in web.xml.
	 * @param project
	 * @param springConfigEntryPaths the values of init-params.
	 * @throws UnsupportedProjectException if the spring misconfigured.
	 */
	public SpringParser(Project project, List<String> springConfigEntryPaths) {
		Expect.notNull(project, "project must not be null.");
		Expect.asTrue(springConfigEntryPaths != null && !springConfigEntryPaths.isEmpty(),
				"Spring config entry paths must not be empty.");
		this.project = project;
		springXmlParsers = new LinkedList<SpringXmlParser>();
		Set<String> tracker = new HashSet<String>();
		try {
			List<String> springPaths = parseConfigLocations(springConfigEntryPaths) ;
			loadSpringConfigFiles(springPaths, tracker);
		} catch (Exception e) {
			throw new UnsupportedProjectException(e);
		}
	}
	
	private List<String> parseConfigLocations(List<String> valueInWebXml) {
		List<String> result = new LinkedList<String>();
		for (String value: valueInWebXml) {
			String[] tokens = value.split("[,\\s]+");
			for (int i = 0; i < tokens.length; i++) {
				if ("".equals(tokens[i])) {
					continue;
				}
				String tmp = tokens[i];
				if ("classpath".equals(tokens[i])) {
					if (i + 2 > tokens.length-1
						|| !":".equals(tokens[i+1])
						|| "".equals(tokens[2])) {
						throw new UnsupportedProjectException("Invalid spring config:" + value);
					}
					tmp = tokens[i] + tokens[++i] + tokens[++i];
				}
				result.add(webxmlConfigPathToProjectPath(tmp));
			}
		}
		return result;
	}
	
	public String getPropertiesFilePath() {
		for (SpringXmlParser parser : springXmlParsers) {
			String val = parser.getPropertiesFile();
			if (val != null) {
				if (!val.trim().startsWith("classpath")) {
					throw new UnsupportedProjectException("imported file must start with classpath.");
				}
				return val;
			}
		}
		return  null;
	}
	
	public String getDatabaseUrl() {
		for (SpringXmlParser parser : springXmlParsers) {
			if (parser.getDbUrl() != null) {
				return parser.getDbUrl();
			}
		}
		throw new UnsupportedProjectException("Failed to find database url.");
	}
	
	public String getDatabaseUsername() {
		for (SpringXmlParser parser : springXmlParsers) {
			if (parser.getDbUsername() != null) {
				return parser.getDbUsername();
			}
		}
		throw new UnsupportedProjectException("Failed to find database username.");
	}
	
	public String getDatabasePassword() {
		for (SpringXmlParser parser : springXmlParsers) {
			if (parser.getDbPassword() != null) {
				return parser.getDbPassword();
			}
		}
		throw new UnsupportedProjectException("Failed to find database password.");
	}
	
	public String getDatabaseDriverClassName() {
		for (SpringXmlParser parser : springXmlParsers) {
			if (parser.getDbDriverClass() != null) {
				return parser.getDbDriverClass();
			}
		}
		throw new UnsupportedProjectException("Failed to find database driver class name.");
	}
	
	
	private Folder findUniqueFolderContainingFile(String filePath) {
		List<Folder> folders = project.findFoldersContainingFile(filePath);
		if (folders.isEmpty()) {
			throw new UnsupportedProjectException("Could not find config file: " + filePath);
		}
		if (folders.size() != 1) {
			throw new UnsupportedProjectException("More than one file found: " + filePath);
		}
		return folders.get(0);
	}
	
	
	private String convertClasspath(String classpath) {
		String[] tokens = classpath.split(":");
		if (tokens.length != 2) {
			return null;
		}
		String prefix = Project.MAVEN_RESOURCES_PATH;
		return prefix + "/" + tokens[1].trim();
	}
	
	private String webxmlConfigPathToProjectPath(String path) {
		String[] tokens = path.split(":");
		String filePath = null;
		String prefix = Project.WEBAPP_PATH_SUFFIX;
		String name = tokens[0].trim();
		if (tokens.length == 2) {
			prefix = Project.MAVEN_RESOURCES_PATH;
			name = tokens[1].trim();
		}
		if (name.startsWith("/")) {
			throw new UnsupportedProjectException("File name must not start with '/': " + path);
		}
		filePath = prefix + "/" + name;
		Folder folder = findUniqueFolderContainingFile(filePath);
		String filename = filePath.replaceFirst("^.*/([^/]+)$", "$1");
		return folder.path() + "/" + filename;
	}
	
	private String springImportPathToProjectPath(String currentSpringConfigPath,
			String importedPath) {
		if (importedPath == null || "".equals(importedPath.trim())) {
			return null;
		}
		importedPath = importedPath.trim();
		if (importedPath.startsWith("classpath")) {
			importedPath = convertClasspath(importedPath);
		}
		String currentFolderPath = FolderUtil.folderPath(currentSpringConfigPath);
		logger.info("current path:{}, filepath:{}.", currentFolderPath, importedPath);
		return currentFolderPath + "/" + importedPath;
	}
	
	/**
	 * Build the project paths of the imported files.
	 * @param currentConfigPath the importing file's path.
	 * @param imported imported paths.
	 * @return project paths of the imported paths.
	 */
	private List<String> handleSpringImportedPaths(String currentConfigPath, List<String> imported) {
		List<String> result = new LinkedList<String>();
		for (String importValue: imported) {
			String tmp = springImportPathToProjectPath(currentConfigPath, importValue);
			if (tmp == null) {
				logger.info("Unsupported file {}, ignored.", importValue);
			} else {
				result.add(tmp);
			}
		}
		return result;
	}

	private Logger logger = LogFactory.getLogger(this.getClass());
	
	private void loadSpringConfigFiles(List<String> fileNames, Set<String> tracker) throws FileNotFoundException, DocumentException, IOException {
		for (String path : fileNames) {
			logger.info("Scan file:{}.", path);
			if (tracker.contains(path)) {
				logger.info("{} detected twice, ignore.", path);
				//Avoid loop.
				continue;
			}
			tracker.add(path);
			InputStream inputStream = project.getInputStream(path);
			try {
				SpringXmlParser xmlParser = new SpringXmlParser(inputStream);
				springXmlParsers.add(xmlParser);
				List<String> newFileNames = handleSpringImportedPaths(path, xmlParser.getImportedConfigFiles());
				if (!newFileNames.isEmpty()) {
					loadSpringConfigFiles(newFileNames, tracker);
				}
			} finally {
				project.closeInputStream(inputStream);
			}
		}
	}
}