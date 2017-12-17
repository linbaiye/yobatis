package org.nalby.yobatis.structure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.DocumentException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.xml.PomXmlParser;

/**
 * Treat all pom files as a whole.
 * @author Kyle Lin
 *
 */
public class PomParser {
	
	private Project project;
	
	private Map<PomXmlParser, Folder> pomXmlParsers;
	
	private Set<String> resourcePaths;
	
	private Set<String> sourceCodePaths;
	
	private Set<String> webappPaths;

	private Logger logger = LogFactory.getLogger(this.getClass());

	public PomParser(Project project) {
		try {
			if (!project.containsFile("pom.xml")) {
				throw new UnsupportedProjectException("Project does not contain root pom.");
			}
			this.project = project;
			parsePomFiles();
		} catch (FileNotFoundException e) {
			throw new UnsupportedProjectException(e);
		} catch (DocumentException e) {
			throw new UnsupportedProjectException(e);
		} catch (IOException e) {
			throw new UnsupportedProjectException(e);
		}
	}
	
	private PomXmlParser getPomXmlParser(String path) throws DocumentException, IOException {
		InputStream inputStream = null;
		try {
			inputStream = project.getInputStream(path);
			return new PomXmlParser(inputStream);
		} finally {
			project.closeInputStream(inputStream);
		}
	}
	
	private void addResourcePaths(PomXmlParser parser, Folder folder) {
		Set<String> dirs = parser.getResourceDirs();
		for (String dir : dirs) {
			if (dir.startsWith("/")) {
				resourcePaths.add(folder.path() + dir);
			} else {
				resourcePaths.add(folder.path() + "/" + dir);
			}
		}
	}
	
	private void addSourceCodePaths(PomXmlParser parser, Folder folder) {
		if (!parser.isContainer()) {
			sourceCodePaths.add(folder.path() + "/src/main/java");
		}
	}

	private void addWebappPaths(PomXmlParser parser, Folder folder) {
		String tmp = parser.getWebappDir();
		if (tmp != null) {
			webappPaths.add(folder.path() + "/" + tmp);
		}
	}
	
	private void parseSubPoms(PomXmlParser parent, Folder parentFolder) throws DocumentException, IOException {
		addResourcePaths(parent, parentFolder);
		addSourceCodePaths(parent, parentFolder);
		addWebappPaths(parent, parentFolder);
		pomXmlParsers.put(parent, parentFolder);

		Set<String> submodules = parent.getModuleNames();
		List<Folder> subfolders = parentFolder.getSubFolders();
		for (String submodule : submodules) {
			for (Folder subfolder: subfolders) {
				if (!submodule.equals(subfolder.name())) {
					continue;
				}
				if (!subfolder.containsFile("pom.xml")) {
					logger.info("Could not find module:{}.", submodule);
					continue;
				}
				PomXmlParser pomXmlParser = getPomXmlParser(subfolder.path() + "/pom.xml");
				parseSubPoms(pomXmlParser, subfolder);
				break;
			}
		}
	}
	
	/**
	 * Get all resource paths, the paths might not exist.
	 * @return the paths.
	 */
	public Set<String> getResourcePaths() {
		return this.resourcePaths;
	}
	
	public Set<String> getSourceCodePaths() {
		return this.sourceCodePaths;
	}
	
	public Set<String> getWebappPaths() {
		return this.webappPaths;
	}
	
	private void parsePomFiles() throws DocumentException, IOException {
		pomXmlParsers = new HashMap<PomXmlParser, Folder>();
		PomXmlParser parser = getPomXmlParser("pom.xml");
		resourcePaths = new HashSet<String>();
		sourceCodePaths = new HashSet<String>();
		webappPaths = new HashSet<String>();
		parseSubPoms(parser, project);
	}
	
	/**
	 * Get property of the active profile.
	 * @param name property name
	 * @return the property value if found, null else.
	 */
	public String getProperty(String name) {
		Expect.notEmpty(name, "name must not be empty.");
		String key = PropertyUtil.valueOfPlaceholder(name);
		for (PomXmlParser parser : pomXmlParsers.keySet()) {
			String tmp = parser.getProperty(key);
			if (tmp != null) {
				return tmp;
			}
		}
		return null;
	}
	
	/**
	 * Get sql connector's jar path based on the {@code driverClassName},
	 * the first <dependency> will be used if multiple found.
	 * @param driverClassName the sql's driver class name.
	 * @return the system path of the connector, null if not found.
	 */
	public String getDatabaseJarPath(String driverClassName) {
		for (PomXmlParser parser : pomXmlParsers.keySet()) {
			String tmp = parser.dbConnectorJarRelativePath(driverClassName);
			if (tmp != null) {
				return project.concatMavenResitoryPath(tmp);
			}
		}
		return null;
	}
}
