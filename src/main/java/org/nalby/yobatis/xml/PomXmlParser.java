package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.util.TextUtil;

public class PomXmlParser extends AbstractXmlParser {

	private static final String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";
	
	private static final String MYSQL_GROUP_ID = "mysql";

	private static final String MYSQL_ARTIFACT_ID = "mysql-connector-java";
	
	private static final String VERSION_TAG = "version";
	
	//The values in <properties></properties> and in <profile></profile>.
	private Map<String, String> properties;

	private String artificatId;

	private String packaging;

	private Set<String> resourceDirs;

	public PomXmlParser(InputStream inputStream)
			throws DocumentException, IOException {
		super(inputStream, "project");
		loadArtificatId();
		loadProperties();
		loadPackaging();
		loadResourceDirs();
	}
	
	public String getArtifactId() {
		return artificatId;
	}

	public boolean artifactIdEquals(String str) {
		return artificatId.equals(str);
	}
	
	private void loadArtificatId() {
		Element root = document.getRootElement();
		if (root.element("artifactId") == null ) {
			throw new UnsupportedProjectException("pom has no artifactId element.");
		}
		artificatId = root.element("artifactId").getTextTrim();
		if (TextUtil.isEmpty(artificatId)) {
			throw new UnsupportedProjectException("artifactId element has no value.");
		}	
	}

	private void loadPackaging() {
		Element e = document.getRootElement().element("packaging");
		if (e != null) {
			packaging = e.getTextTrim();
			if ("".equals(packaging)) {
				throw new UnsupportedProjectException("packaging element has no value.");
			}
		}
	}

	private String filterDirectoryValue(Element directoryElement) {
		if (directoryElement == null) {
			return null;
		}
		String directoryText = directoryElement.getTextTrim();
		if (TextUtil.isEmpty(directoryText)) {
			return null;
		}
		List<String> placeholders = PropertyUtil.placeholdersFrom(directoryText);
		for (String placeholder: placeholders) {
			String value = properties.get(PropertyUtil.valueOfPlaceholder(placeholder));
			if (value != null) {
				directoryText = directoryText.replace(placeholder, value);
			}
		}
		return directoryText;
	}
	
	private void loadResourceDirs() {
		resourceDirs = new HashSet<String>();
		if (packaging == null) {
			return;
		}
		resourceDirs.add("src/main/resources");
		Element build = document.getRootElement().element("build");
		if (build == null) {
			return;
		}
		Element resources = build.element("resources");
		if (resources == null) {
			return;
		}
		List<Element> resourceList = resources.elements("resource");
		for (Element resource: resourceList) {
			Element directoryElement = resource.element("directory");
			String directory = filterDirectoryValue(directoryElement);
			if (directory != null) {
				resourceDirs.add(directory);
			}
		}
	}


	private void loadProperties() {
		properties = new HashMap<String, String>();
		Element root = document.getRootElement();
		Element propertiesElement = root.element("properties");
		if (propertiesElement != null) {
			for (Element e: propertiesElement.elements()) {
				if (!TextUtil.isEmpty(e.getTextTrim())) {
					properties.put(e.getName().trim(), e.getTextTrim());
				}
			}
		}
		loadProfileProperties();
	}

	private boolean isProfileActive(Element profileElement) {
		Element activation = profileElement.element("activation");
		if (activation == null) {
			return false;
		}
		Element activeByDefault = activation.element("activeByDefault");
		if (activeByDefault == null || activeByDefault.getText() == null) {
			return false;
		}
		return "true".equals(activeByDefault.getTextTrim());
	}

	private void loadProfileProperties() {
		Element root = document.getRootElement();
		Element profilesElement = root.element("profiles");
		if (profilesElement == null) {
			return;
		}
		List<Element> profileElements = profilesElement.elements("profile");
		for (Element profileElement : profileElements) {
			if (!isProfileActive(profileElement)) {
				continue;
			}
			Element propertiesElement = profileElement.element("properties");
			for (Element property: propertiesElement.elements()) {
				if (property.getText() != null && !"".equals(property.getTextTrim())) {
					properties.put(property.getName().trim(), property.getTextTrim());
				}
			}
		}
	}
	
	private boolean isMysqlDependency(Element dependencyElement) {
		Element groupIdElement = dependencyElement.element("groupId");
		Element artifactIdElement = dependencyElement.element("artifactId");
		Element versionElement = dependencyElement.element(VERSION_TAG);
		if (groupIdElement == null || artifactIdElement == null || versionElement == null) {
			return false;
		}
		String groupId = groupIdElement.getTextTrim();
		String artifact = artifactIdElement.getTextTrim();
		String version = versionElement.getTextTrim();
		if (!MYSQL_GROUP_ID.equals(groupId) || !MYSQL_ARTIFACT_ID.equals(artifact)
				|| version == null || "".equals(version)) {
			return false;
		}
		return true;
	}

	private String glueVersion(Element dependencyElement) {
		Element versionElement = dependencyElement.element(VERSION_TAG);
		if (versionElement == null) {
			return null;
		}
		String version = versionElement.getTextTrim();
		if (PropertyUtil.isPlaceholder(version)) {
			version = getProperty(PropertyUtil.valueOfPlaceholder(version));
		}
		if (TextUtil.isEmpty(version)) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(MYSQL_GROUP_ID)
		.append("/")
		.append(MYSQL_ARTIFACT_ID)
		.append("/")
		.append(version)
		.append("/")
		.append(MYSQL_ARTIFACT_ID)
		.append("-")
		.append(version)
		.append(".jar");
		return builder.toString();
	}
	
	private String buildMysqlJarRelativePath() {
		Element root = document.getRootElement();
		Element dependenciesElement = root.element("dependencies");
		Element dependencyManagementElement = root.element("dependencyManagement");
		if (dependencyManagementElement != null) {
			dependenciesElement = dependencyManagementElement.element("dependencies");
		}
		if (dependenciesElement == null) {
			return null;
		}
		for (Element dependencyElement: dependenciesElement.elements("dependency")) {
			if (!isMysqlDependency(dependencyElement)) {
				continue;
			}
			return glueVersion(dependencyElement);
		}
		return null;
	}
	
	/**
	 * Get sql connector's jar path based on the {@code driverClassName},
	 * the first <dependency> will be used if multiple found.
	 * @param driverClassName the sql's driver class name.
	 * @return the relative path of the connector.
	 */
	public String dbConnectorJarRelativePath(String driverClassName) {
		if (MYSQL_DRIVER_CLASS.equals(driverClassName)) {
			return buildMysqlJarRelativePath();
		}
		return null;
	}
	
	/**
	 * Get profile property.
	 * @param name property name
	 * @return the property if found, null else.
	 */
	public String getProperty(String name) {
		Expect.notEmpty(name, "name must not be empty.");
		return properties.get(name);
	}
	
	public Set<String> getResourceDirs() {
		return resourceDirs;
	}
}
