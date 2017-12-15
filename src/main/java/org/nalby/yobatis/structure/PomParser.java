package org.nalby.yobatis.structure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.DocumentException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.structure.Project.FolderSelector;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.xml.PomXmlParser;

/**
 * Treat all pom files as a whole.
 * @author Kyle Lin
 *
 */
public class PomParser {
	
	private PomXmlParser rootPom;

	private Project project;
	
	private List<PomXmlParser> pomXmlParsers;
	
	private static final String DEFAULT_CLASSPATH_SUBSTR = "src/main/resources";

	public PomParser(Project project) {
		try {
			this.project = project;
			this.pomXmlParsers = new LinkedList<PomXmlParser>();
			loadPomFiles();
		} catch (FileNotFoundException e) {
			throw new UnsupportedProjectException(e);
		} catch (DocumentException e) {
			throw new UnsupportedProjectException(e);
		} catch (IOException e) {
			throw new UnsupportedProjectException(e);
		}
	}
	
	private void loadPomFiles() throws DocumentException, IOException {
		if (!project.containsFile("pom.xml")) {
			throw new UnsupportedProjectException("Project does not contain root pom.");
		}
		InputStream inputStream = project.getInputStream("pom.xml");
		rootPom = new PomXmlParser(inputStream);
		pomXmlParsers.add(rootPom);
		project.closeInputStream(inputStream);
		List<Folder> folders = project.findFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return folder.containsFile("pom.xml");
			}
		});
		for (Folder folder: folders) {
			String path = folder.path() + "/pom.xml";
			inputStream = project.getInputStream(path);
			PomXmlParser parser = new PomXmlParser(inputStream);
			project.closeInputStream(inputStream);
			if (!rootPom.artifactIdEquals(parser.getArtifactId())
					&& parser.artifactIdEquals(folder.name())) {
				pomXmlParsers.add(parser);
			}
		}
	}
	
	/**
	 * Get property of the active profile.
	 * @param name property name
	 * @return the property value if found, null else.
	 */
	public String getProperty(String name) {
		Expect.notEmpty(name, "name must not be empty.");
		String key = PropertyUtil.valueOfPlaceholder(name);
		for (PomXmlParser parser : pomXmlParsers) {
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
		for (PomXmlParser parser : pomXmlParsers) {
			String tmp = parser.dbConnectorJarRelativePath(driverClassName);
			if (tmp != null) {
				return project.concatMavenResitoryPath(tmp);
			}
		}
		return null;
	}
}
