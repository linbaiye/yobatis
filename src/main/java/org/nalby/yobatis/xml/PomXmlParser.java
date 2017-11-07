package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.DocumentException;
import org.dom4j.Element;

public class PomXmlParser extends BasicXmlParser {

	private static final String MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";
	
	private static final String MYSQL_GROUP_ID = "mysql";

	private static final String MYSQL_ARTIFACT_ID = "mysql-connector-java";
	
	private static final String VERSION_TAG = "version";
	
	private Map<String, String> profileProperties;
	
	public PomXmlParser(InputStream inputStream)
			throws DocumentException, IOException {
		super(inputStream, "project");
		profileProperties = new HashMap<String, String>();
		loadProfileProperties();
	}
	
	private boolean isActive(Element profileElement) {
		Element activation = profileElement.element("activation");
		if (activation == null) {
			return false;
		}
		Element activeByDefault = activation.element("activeByDefault");
		if (activeByDefault == null || activeByDefault.getText() == null) {
			return false;
		}
		return "true".equals(activeByDefault.getText().trim());
	}
	
	private void loadProfileProperties() {
		Element root = document.getRootElement();
		Element profilesElement = root.element("profiles");
		if (profilesElement == null) {
			return;
		}
		List<Element> profileElements = profilesElement.elements("profile");
		for (Element profileElement : profileElements) {
			if (!isActive(profileElement)) {
				continue;
			}
			Element propertiesElement = profileElement.element("properties");
			for (Element property: propertiesElement.elements()) {
				if (property.getText() != null && !"".equals(property.getText().trim())) {
					profileProperties.put(property.getName(), property.getText().trim());
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
	
	private String getDefinedVersion(String versionPlaceholder) {
		Pattern pattern = Pattern.compile("\\$\\{([\\.a-zA-z1-9]+)\\}");
		Matcher matcher = pattern.matcher(versionPlaceholder);
		if (!matcher.find()) {
			return null;
		}
		String version = matcher.group(1);
		Element element = document.getRootElement().element("properties");
		if (element == null) {
			return null;
		}
		for (Iterator<Element> iterator = element.elementIterator(); iterator.hasNext(); ) {
			Element property = iterator.next();
			if (version.equals(property.getName())) {
				return property.getTextTrim();
			}
		}
		return null;
	}

	private String glueVersion(Element dependencyElement) {
		Element versionElement = dependencyElement.element(VERSION_TAG);
		String version = versionElement.getTextTrim();
		if (version.startsWith("${")) {
			version = getDefinedVersion(version);
		}
		if (version == null) {
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
	public String getProfileProperty(String name) {
		if (profileProperties.containsKey(name)) {
			return profileProperties.get(name);
		}
		return null;
	}
}
