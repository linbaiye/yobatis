package org.nalby.yobatis.structure;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.nalby.yobatis.util.Expect;

public class PropertiesParser {
	private Map<String, String> valuedProperties;

	public PropertiesParser(Project project, Set<String> locations) {
		this.valuedProperties = new HashMap<String, String>();
		loadPropertiesFiles(project, locations);
	}
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	private static final String PLACEHOLDER_PATTERN_STRING = "^\\s*\\$\\{([^\\}]*)\\}\\s*$";

	public static boolean isPlaceholder(String str) {
		if (str == null) {
			return false;
		}
		return Pattern.matches(PLACEHOLDER_PATTERN_STRING, str);
	}
	
	public static String valueOfPlaceholder(String str) {
		if (!isPlaceholder(str)) {
			return str;
		}
		String tmp = str.replaceFirst(PLACEHOLDER_PATTERN_STRING, "$1");
		return tmp.trim();
	}
	
	private void loadPropertiesFiles(Project project, Set<String> locations) {
		for (String location: locations) {
			InputStream inputStream = null;
			try {
				Properties properties = new Properties();
				inputStream = project.getInputStream(location);
				properties.load(inputStream);
				Set<String> names = properties.stringPropertyNames();
				for (String name: names) {
					String value = properties.getProperty(name);
					if (value == null) {
						logger.info("Discard property {}.", name);
						continue;
					}
					valuedProperties.put(name, value.trim());
				}
			} catch (IOException e) {
				logger.info("Failed to load properties file:{}.", location);
			} finally {
				project.closeInputStream(inputStream);
			}
		}
	}
	
	/**
	 * Get the value of property defined in the .properties file.
	 * @param name the property name
	 * @return the value if found, {@code null} else.
	 */
	public String getProperty(String name) {
		Expect.notNull(name, "name must not be null.");
		String key = valueOfPlaceholder(name);
		return valuedProperties.get(key);
	}
}
