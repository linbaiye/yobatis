package org.nalby.yobatis.structure;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nalby.yobatis.exception.UnsupportedProjectException;

public class DatabasePropertiesParser {
	
	private PomParser pomParser;

	private Properties properties;

	private Map<String, String> valuedProperties;

	private SpringParser springParser;

	public DatabasePropertiesParser(Project project, PomParser pomParser, SpringParser springParser) {
		//String filepath = springParser.getPropertiesFilePath();
		String filepath = null;
		try {
			this.pomParser = pomParser;
			this.properties = new Properties();
			this.valuedProperties = new HashMap<String, String>();
			this.springParser = springParser;
			if (filepath == null) {
				return;
			}
			InputStream inputStream = project.getInputStream(filepath);
			this.properties.load(inputStream);
			project.closeInputStream(inputStream);
			replaceProperties();
		} catch (IOException e) {
			throw new UnsupportedProjectException("Failed to open:" + filepath != null? filepath : "");
		}
	}
	
	private static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");
	
	//${key} -> key
	private String placeholderName(String placeholder) {

		Matcher matcher = PATTERN.matcher(placeholder);
		if (!matcher.find()) {
			throw new UnsupportedProjectException("Invalid placeholder:" + placeholder);
		}
		return matcher.group(1);
	}
	
	private void replaceProperties() {
		Set<String> names = properties.stringPropertyNames();
		for (String name: names) {
			String value = properties.getProperty(name);
			if (value.startsWith("${") && value.endsWith("}")) {
				value = pomParser.getProfileProperty(placeholderName(value));
			}
			if (value == null) {
				continue;
			}
			valuedProperties.put(name, value);
		}
	}
	
	/**
	 * Get the value of property defined in the .properties file.
	 * @param name the property name
	 * @return the value if found, {@code null} else.
	 */
	private String getProperty(String name) {
		if (!name.startsWith("${")) {
			return name;
		}
		name = placeholderName(name);
		return valuedProperties.get(name);
	}
	
	public String getDatabaseUrl() {
		String val = springParser.getDatabaseUrl();
		if (val == null) {
			throw new UnsupportedProjectException("Failed to find database url.");
		}
		return getProperty(val);
	}
	
	public String getDatabaseUsername() {
		String val = springParser.getDatabaseUsername();
		if (val == null) {
			throw new UnsupportedProjectException("Failed to find database username.");
		}
		return getProperty(val);
	}
	
	public String getDatabasePassword() {
		String val = springParser.getDatabasePassword();
		if (val == null) {
			throw new UnsupportedProjectException("Failed to find database password.");
		}
		return getProperty(val);
	}
	
	public String getDatabaseDriverClassName() {
		String val = springParser.getDatabaseDriverClassName();
		if (val == null) {
			throw new UnsupportedProjectException("Failed to find database driver class name.");
		}
		return getProperty(val);
	}
}
