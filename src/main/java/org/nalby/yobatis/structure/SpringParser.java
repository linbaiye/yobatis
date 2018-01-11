package org.nalby.yobatis.structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.util.TextUtil;
/**
 * Used to parse spring configuration files. The main purpose is to locate
 * database's properties by parsing imported spring xml files and properties
 * files. It's poorly designed for now and should be optimized in future.
 * 
 * @author Kyle Lin
 */
public class SpringParser {

	private Map<String, String> valuedProperties;
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	private OldSpringAntPatternFileManager fileManager;
	
	private String dbPassword;
	
	private String dbUsername;

	private String dbUrl;
	
	private String dbDriverClassName;
	


	/**
	 * Construct a {@code SpringParser} that analyzes the spring files.
	 * @param fileManager {@link OldSpringAntPatternFileManager}
	 * @param initParamValues The param-value in web.xml, including servlet and application
	 * context.
	 */
	public SpringParser(OldSpringAntPatternFileManager fileManager, Set<String> initParamValues) {
		Expect.notNull(fileManager, "pomParser must not be null.");
		Expect.notNull(initParamValues, "initParamValues must not be null.");
		Set<String> locations = parseLocationsInInitParamValues(initParamValues);
		if (locations.isEmpty()) {
			throw new UnsupportedProjectException("Failed to find spring config files.");
		}
		this.fileManager = fileManager;
		Set<String> files = findSpringFilesConfiguredInWebxml(locations);
		valuedProperties = new HashMap<>();
		iterateFiles(files, new HashSet<String>(), new HashSet<String>());
	}
	
	
	private Set<String> findSpringFilesConfiguredInWebxml(Set<String> hints) {
		Set<String> paths = new HashSet<>();
		for (String hint: hints) {
			paths.addAll(fileManager.findSpringFiles(hint));
		}
		return paths;
	}
	
	
	private void addProperties(Set<String> paths, Set<String> parsedPaths) {
		for (String path: paths) {
			if (parsedPaths.contains(path)) {
				continue;
			}
			parsedPaths.add(path);
			logger.info("Scanning properties file:{}.", path);
			Properties properties = fileManager.readProperties(path);
			if (properties != null) {
				for (String key : properties.stringPropertyNames()) {
					valuedProperties.put(key, properties.getProperty(key));
				}
			}
		}
	}
	
	
	private void iterateFiles(Set<String> files, Set<String> parsedSpringFiles,
			Set<String> parsedPropertiesFiles) {
		for (String file : files) {
			if (parsedSpringFiles.contains(file)) {
				continue;
			}
			logger.info("Scanning spring xml file:{}.", file);
			parsedSpringFiles.add(file);
			if (TextUtil.isEmpty(dbPassword)) {
				dbPassword = fileManager.lookupPropertyOfSpringFile(file, "password");
			}
			if (TextUtil.isEmpty(dbUsername)) {
				dbUsername = fileManager.lookupPropertyOfSpringFile(file, "username");
			}
			if (TextUtil.isEmpty(dbDriverClassName)) {
				dbDriverClassName = fileManager.lookupPropertyOfSpringFile(file, "driverClassName");
			}
			if (TextUtil.isEmpty(dbUrl)) {
				dbUrl = fileManager.lookupPropertyOfSpringFile(file, "url");
			}
			Set<String> proprtiesPaths = fileManager.findPropertiesFiles(file);
			addProperties(proprtiesPaths, parsedPropertiesFiles);
			Set<String> newFiles = fileManager.findImportSpringXmlFiles(file);
			iterateFiles(newFiles, parsedSpringFiles, parsedPropertiesFiles);
		}
	}
	
	
	private Set<String> parseLocationsInInitParamValues(Set<String> initParamValues) {
		Set<String> result = new HashSet<String>();
		for (String paramValue : initParamValues) {
			String[] locations = paramValue.split("[,\\s]+");
			for (int i = 0; i < locations.length; i++) {
				if ("".equals(locations[i])) {
					continue;
				}
				String tmp = locations[i];
				if (tmp.startsWith("classpath:") || tmp.startsWith("classpath*:")) {
					if (!tmp.matches("classpath\\*?:.+")) {
						logger.info("Invalid hint:{}.", tmp);
						break;
					}
				}
				result.add(tmp);
			}
		}
		return result;
	}
	
	
	private String filterPlaceholders(String text) {
		List<String> placeholders = PropertyUtil.placeholdersFrom(text);
		for (String placeholder : placeholders) {
			String key = PropertyUtil.valueOfPlaceholder(placeholder);
			String val = valuedProperties.get(key);
			if (val != null) {
				text = text.replace(placeholder, val);
			}
		}
		return text;
	}
	

	/**
	 * Get the database's url configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be found anywhere.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabaseUrl() {
		return filterPlaceholders(dbUrl);
	}
	
	/**
	 * Get the database's username configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be found anywhere.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabaseUsername() {
		return filterPlaceholders(dbUsername);
	}
	
	/**
	 * Get the database's password configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be found anywhere.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabasePassword() {
		return filterPlaceholders(dbPassword);
	}
	
	/**
	 * Get the database's driver class name configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be found anywhere.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabaseDriverClassName() {
		return filterPlaceholders(dbDriverClassName);
	}
}