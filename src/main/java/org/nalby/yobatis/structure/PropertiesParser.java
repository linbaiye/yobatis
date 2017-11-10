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

public class PropertiesParser {
	
	private PomParser pomParser;

	private Properties properties;

	private Map<String, String> valuedProperties;
	
	public PropertiesParser(Project project, PomParser pomParser, String filepath) {
		try {
			this.pomParser = pomParser;
			this.properties = new Properties();
			this.valuedProperties = new HashMap<String, String>();
			if (filepath == null) {
				return;
			}
			InputStream inputStream = project.getInputStream(filepath);
			this.properties.load(inputStream);
			project.closeInputStream(inputStream);
			replaceProperties();
		} catch (IOException e) {
			throw new UnsupportedProjectException("Failed to open:" + filepath);
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
				throw new UnsupportedProjectException("Unable to find property of  '" + name + "'");
			}
			valuedProperties.put(name, value);
		}
	}
	
	/**
	 * Get the value of property defined in the .properties file.
	 * @param name the property name
	 * @return the value if found, {@code null} else.
	 */
	public String getProperty(String name) {
		if (name.startsWith("${") && name.endsWith("}")) {
			name = placeholderName(name);
		}
		return name == null? null : valuedProperties.get(name);
	}
}
