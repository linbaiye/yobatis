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
 * The main purpose is to search database's properties by parsing imported spring xml 
 * files and properties files. 
 * 
 * @author Kyle Lin
 */
public final class SpringParser {

	private Map<String, String> valuedProperties;
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	private SpringAntPathFileManager fileManager;
	
	private String dbPassword;
	
	private String dbUsername;

	private String dbUrl;
	
	private String dbDriverClassName;
	

	/**
	 * Construct a {@code SpringParser} that analyzes the spring files.
	 * @param fileManager {@link SpringAntPathFileManager}
	 * @param initParamValues The param-value in web.xml, including servlet and application
	 * context.
	 */
	public SpringParser(SpringAntPathFileManager fileManager, Set<String> initParamValues) {
		Expect.notNull(fileManager, "pomParser must not be null.");
		Expect.notNull(initParamValues, "initParamValues must not be null.");
		Set<String> locations = parseLocationsInInitParamValues(initParamValues);
		if (locations.isEmpty()) {
			throw new UnsupportedProjectException("Failed to find spring config files.");
		}
		this.fileManager = fileManager;
		Set<File> files = findSpringFilesConfiguredInWebxml(locations);
		valuedProperties = new HashMap<>();
		iterateFiles(files, new HashSet<File>(), new HashSet<File>());
	}
	
	
	private Set<File> findSpringFilesConfiguredInWebxml(Set<String> hints) {
		Set<File> paths = new HashSet<>();
		for (String hint: hints) {
			paths.addAll(fileManager.findSpringFiles(hint));
		}
		return paths;
	}
	
	
	private void addProperties(Set<File> files, Set<File> parsedFiles) {
		for (File file: files) {
			if (parsedFiles.contains(file)) {
				continue;
			}
			parsedFiles.add(file);
			logger.info("Scanning properties file:{}.", file.path());
			Properties properties = fileManager.readProperties(file);
			if (properties != null) {
				for (String key : properties.stringPropertyNames()) {
					valuedProperties.put(key, properties.getProperty(key));
				}
			}
		}
	}
	
	
	private void iterateFiles(Set<File> files, Set<File> parsedSpringFiles,
			Set<File> parsedPropertiesFiles) {
		for (File file : files) {
			if (parsedSpringFiles.contains(file)) {
				continue;
			}
			logger.info("Scanning spring xml file:{}.", file.path());
			parsedSpringFiles.add(file);
			if (TextUtil.isEmpty(dbPassword)) {
				dbPassword = fileManager.lookupDbProperty(file, "password");
			}
			if (TextUtil.isEmpty(dbUsername)) {
				dbUsername = fileManager.lookupDbProperty(file, "username");
			}
			if (TextUtil.isEmpty(dbDriverClassName)) {
				dbDriverClassName = fileManager.lookupDbProperty(file, "driverClassName");
			}
			if (TextUtil.isEmpty(dbUrl)) {
				dbUrl = fileManager.lookupDbProperty(file, "url");
			}
			Set<File> proprtiesFiles = fileManager.findPropertiesFiles(file);
			addProperties(proprtiesFiles, parsedPropertiesFiles);
			Set<File> newFiles = fileManager.findSpringFiles(file);
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
	
	
	/**
	 * Properties configured in spring files are likely to be placeholders, so checking in
	 * properties files is necessary.
	 * @param text
	 * @return
	 */
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
