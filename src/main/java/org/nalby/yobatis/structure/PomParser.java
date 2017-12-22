package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.xml.PomXmlParser;

/**
 * Treat all pom files as a whole.
 * @author Kyle Lin
 */
public class PomParser {
	
	private Project project;
	
	private Map<PomXmlParser, Folder> pomXmlParsers;
	
	private Set<String> sourceCodePaths;
	
	private Folder webappFolder;
	
	private Set<Folder> resourceFolders;

	private Logger logger = LogFactory.getLogger(this.getClass());

	public PomParser(Project project) {
		try {
			if (!project.containsFile("pom.xml")) {
				throw new UnsupportedProjectException("Project does not contain root pom.");
			}
			this.project = project;
			parsePomFiles();
		} catch (Exception e) {
			throw new UnsupportedProjectException(e);
		}
	}
	
	private PomXmlParser getPomXmlParser(String path) throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = project.getInputStream(path);
			return new PomXmlParser(inputStream);
		} finally {
			project.closeInputStream(inputStream);
		}
	}
	
	private void addSourceCodePaths(PomXmlParser parser, Folder folder) {
		if (!parser.isContainer()) {
			sourceCodePaths.add(folder.path() + "/src/main/java");
		}
	}

	private void setWebappFolder(PomXmlParser parser, Folder folder) {
		if (!parser.isPackagingWar()) {
			return;
		}
		if (webappFolder == null) {
			try {
				webappFolder = project.findFolder(folder.path() + "/src/main/webapp");
			} catch (Exception e) {
				//ignore.
			}
		}
	}
	
	private void addResourceFolders(PomXmlParser parser, Folder folder) {
		Set<String> dirs = parser.getResourceDirs();
		for (String dir : dirs) {
			String path = FolderUtil.concatPath(folder.path(), dir);
			try {
				resourceFolders.add(project.findFolder(path));
			} catch (Exception e) {
				//ignore.
			}
		}
	}
	
	private void parseSubPoms(PomXmlParser parent, Folder parentFolder) throws Exception {
		addResourceFolders(parent, parentFolder);
		addSourceCodePaths(parent, parentFolder);
		setWebappFolder(parent, parentFolder);
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
	
	public Set<Folder> getResourceFolders() {
		return this.resourceFolders;
	}
	
	public Set<String> getSourceCodePaths() {
		return this.sourceCodePaths;
	}
	
	public Folder getWebappFolder() {
		if (webappFolder == null) {
			throw new UnsupportedProjectException("project does not have a webapp dir.");
		}
		return webappFolder;
	}
	
	private void parsePomFiles() throws Exception {
		pomXmlParsers = new HashMap<PomXmlParser, Folder>();
		PomXmlParser parser = getPomXmlParser("pom.xml");
		sourceCodePaths = new HashSet<String>();
		resourceFolders = new HashSet<Folder>();
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
	 * Filter placeholders in {@code string} if any of them is defined
	 * in pom files.
	 * @param string the string to filter.
	 * @return the string filtered.
	 */
	public String filterPlaceholders(String string) {
		Expect.notNull(string, "string must not be empty.");
		List<String> placeholders = PropertyUtil.placeholdersFrom(string);
		for (String placeholder: placeholders) {
			String value = getProperty(placeholder);
			if (value != null) {
				string = string.replace(placeholder, value);
			}
		}
		return string;
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
