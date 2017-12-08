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
			List<String> springPaths = parseLocationsInWebXml(springConfigEntryPaths) ;
			loadSpringConfigFiles(springPaths, tracker);
		} catch (Exception e) {
			throw new UnsupportedProjectException(e);
		}
	}
	
	private List<String> parseLocationsInWebXml(List<String> valueInWebXml) {
		List<String> result = new LinkedList<String>();
		for (String value: valueInWebXml) {
			String[] tokens = value.split("[,\\s]+");
			for (int i = 0; i < tokens.length; i++) {
				if ("".equals(tokens[i])) {
					continue;
				}
				if ("classpath".equals(tokens[i])) {
					if (i + 2 > tokens.length-1
						|| !":".equals(tokens[i+1])
						|| "".equals(tokens[2])) {
						throw new UnsupportedProjectException("Invalid spring config:" + value);
					}
					result.add(tokens[i] + tokens[++i] + tokens[++i]);
				} else {
					result.add(tokens[i]);
				}
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
				return configPathToProjectPath(val);
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
	
	private String configPathToProjectPath(String path) {
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
		List<Folder> folders = project.findFoldersContainingFile(filePath);
		if (folders.isEmpty()) {
			throw new UnsupportedProjectException("Could not find config file: " + path);
		}
		if (folders.size() != 1) {
			throw new UnsupportedProjectException("More than one file found: " + path);
		}
		Folder folder = folders.get(0);
		String filename = filePath.replaceFirst("^.*/([^/]+)$", "$1");
		return folder.path() + "/" + filename;
	}

	
	private void loadSpringConfigFiles(List<String> fileNames, Set<String> tracker) throws FileNotFoundException, DocumentException, IOException {
		for (String path : fileNames) {
			if (tracker.contains(path)) {
				//Avoid loop.
				continue;
			}
			tracker.add(path);
			String fspath = configPathToProjectPath(path);
			InputStream inputStream = project.getInputStream(fspath);
			try {
				SpringXmlParser xmlParser = new SpringXmlParser(inputStream);
				springXmlParsers.add(xmlParser);
				List<String> newFileNames  = xmlParser.getImportedConfigFiles();
				if (!newFileNames.isEmpty()) {
					loadSpringConfigFiles(newFileNames, tracker);
				}
			} finally {
				project.closeInputStream(inputStream);
			}

		}
	}
}