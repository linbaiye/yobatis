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

	public SpringParser(Project project, List<String> springConfigEntryPaths) {
		Expect.notNull(project, "project must not be null.");
		Expect.asTrue(springConfigEntryPaths != null && !springConfigEntryPaths.isEmpty(),
				"Spring config entry paths must not be empty.");
		this.project = project;
		springXmlParsers = new LinkedList<SpringXmlParser>();
		Set<String> tracker = new HashSet<String>();
		try {
			loadSpringConfigFiles(springConfigEntryPaths, tracker);
		} catch (Exception e) {
			throw new UnsupportedProjectException(e);
		}
	}
	
	public String getPropertiesFilePath() {
		for (SpringXmlParser parser : springXmlParsers) {
			String val = parser.getPropertiesFile();
			if (val != null) {
				return convertClassPathToProjectPath(val);
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


	private String convertClassPathToProjectPath(String path) {
		String[] tokens = path.split(":");
		if (tokens.length != 2)  {
			throw new UnsupportedProjectException("invalid spring config file path: " + path + "', format should be classpath:filename" );
		}
		List<Folder> folders = project.findFoldersContainingFile(tokens[1]);
		if (folders.isEmpty()) {
			throw new UnsupportedProjectException("Could not find config file: " + path);
		}
		if (folders.size() != 1) {
			throw new UnsupportedProjectException("More than one file found: " + path);
		}
		Folder folder = folders.get(0);
		String filename = tokens[1];
		if (filename.indexOf("/") != -1) {
			//remove duplicated paths.
			String folderPath  = tokens[1].replaceFirst("/.*$", "");
			filename = tokens[1].replace(folderPath + "/", "");
		}
		return folder.path() + "/" + filename;
	}

	
	private void loadSpringConfigFiles(List<String> fileNames, Set<String> tracker) throws FileNotFoundException, DocumentException, IOException {
		for (String path : fileNames) {
			if (tracker.contains(path)) {
				//Avoid loop.
				continue;
			}
			tracker.add(path);
			String fspath = convertClassPathToProjectPath(path);
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