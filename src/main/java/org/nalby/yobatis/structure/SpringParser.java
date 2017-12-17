package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.xml.SpringXmlParser;
/**
 * Used to parse spring configuration files. The main purpose is to locate
 * database's properties by parsing imported spring xml files, and properties
 * files configured.
 * 
 * @author Kyle Lin
 */
public class SpringParser {

	private Project project;

	private List<SpringXmlParser> springXmlParsers;
	
	private Set<String> propertiesFileLocations;
	
	private Set<String> webappPaths;

	private Set<String> resourcePaths;

	private Map<String, String> valuedProperties;

	private Logger logger = LogFactory.getLogger(this.getClass());
	
	/**
	 * Construct a {@code SpringParser} that analyzes the spring files.
	 * @param project see {@link org.nalby.yobatis.structure.Project}
	 * <p>
	 * @param resourcePaths The paths that 'classpath' fits.
	 * <p>
	 * @param webappPaths The projects' webapp paths, should be only one currently.
	 * <p>
	 * @param initParamValues The param-value in web.xml, including servlet and application
	 * context.
	 */
	public SpringParser(Project project, Set<String> resourcePaths, 
			Set<String> webappPaths, Set<String> initParamValues) {
		Expect.notNull(project, "project must not be null.");
		Expect.notNull(initParamValues,  "initParamValues must not be null.");
		Expect.notNull(resourcePaths,  "resourcePaths must not be null.");
		Expect.notNull(webappPaths,  "webappPaths must not be null.");
		Set<String> locations = parseLocationsInInitParamValues(initParamValues);
		if (locations.isEmpty()) {
			throw new UnsupportedProjectException("Failed to find spring config files.");
		}
		this.project = project;
		this.webappPaths = webappPaths;
		this.resourcePaths = resourcePaths;
		parseSpringXmlFiles(locations);
	}
	
	private Set<String> parseLocationsInInitParamValues(Set<String> initParamValues) {
		Set<String> locations = new HashSet<String>();
		for (String paramValue : initParamValues) {
			String[] tokens = paramValue.split("[,\\s]+");
			for (int i = 0; i < tokens.length; i++) {
				if ("".equals(tokens[i])) {
					continue;
				}
				String tmp = tokens[i];
				if ("classpath".equals(tokens[i])) {
					if (i + 2 > tokens.length-1
						|| !":".equals(tokens[i+1])
						|| "".equals(tokens[2])) {
						logger.info("Ignore location:{}.", paramValue);
						break;
					}
					tmp = tokens[i] + tokens[++i] + tokens[++i];
				}
				locations.add(tmp);
			}
		}
		return locations;
	}
	
	private SpringXmlParser buildSpringXmlParser(String path) {
		InputStream inputStream = null;
		try {
			inputStream = project.getInputStream(path);
			return new SpringXmlParser(inputStream);
		} catch (Exception e) {
			return null;
		} finally {
			project.closeInputStream(inputStream);
		}
	}
	
	private void addProperties(String path) {
		InputStream inputStream = null;
		try {
			Properties properties = new Properties();
			inputStream = project.getInputStream(path);
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
		} catch (Exception e) {
			logger.info("Failed to load properties file:{}.", path);
		} finally {
			project.closeInputStream(inputStream);
		}
	}
	
	private void parserPropertiesFiles(String thisXmlPath, SpringXmlParser xmlParser) {
		Set<String> locations = xmlParser.getPropertiesFileLocations();
		for (String location: locations) {
			if (location.startsWith("classpath")) {
				String tokens[] = location.split(":");
				for (String basepath : this.resourcePaths) {
					String path = FolderUtil.concatPath(basepath, tokens[1]);
					addProperties(path);
				}
			} else {
				String basepath = FolderUtil.folderPath(thisXmlPath);
				addProperties(FolderUtil.concatPath(basepath, location));
			}
		}
	}
	
	private SimpleEntry<String, SpringXmlParser> 
		buildSpringXmlParser(Set<String> basePaths, String location) {
		for (String basePath : basePaths) {
			String tmp = FolderUtil.concatPath(basePath, location);
			SpringXmlParser xmlParser = buildSpringXmlParser(tmp);
			if (xmlParser != null) {
				return new SimpleEntry<String, SpringXmlParser>(tmp, xmlParser);
			}
		}
		return new SimpleEntry<String, SpringXmlParser>(null, null);
	}
	
	private void parseXmlFile(String thisXmlPath, SpringXmlParser parser, Set<String> parsedPaths) {
		if (parsedPaths.contains(thisXmlPath)) {
			//In case of loop.
			return;
		}
		parsedPaths.add(thisXmlPath);
		springXmlParsers.add(parser);
		parserPropertiesFiles(thisXmlPath, parser);
		for (String location: parser.getImportedLocations()) {
			SpringXmlParser springXmlParser = null;
			String nextPath = null;
			if (location.startsWith("classpath")) {
				String tokens[] = location.split(":");
				SimpleEntry<String, SpringXmlParser> kv = 
						buildSpringXmlParser(this.resourcePaths, tokens[1]);
				springXmlParser = kv.getValue();
				nextPath = kv.getKey();
			} else {
				String thisXmlDirPath = FolderUtil.folderPath(thisXmlPath);
				nextPath = FolderUtil.concatPath(thisXmlDirPath, location);
				springXmlParser = buildSpringXmlParser(nextPath);
			}
			if (springXmlParser != null) {
				parseXmlFile(nextPath, springXmlParser, parsedPaths);
			}
		}
	}
	
	/**
	 * Parse spring's config files according to the param-value(s) in web.xml,
	 */
	//might need to think about the scale.
	private void parseSpringXmlFiles(Set<String> locations) {
		springXmlParsers = new LinkedList<SpringXmlParser>();
		propertiesFileLocations = new HashSet<String>();
		valuedProperties = new HashMap<String, String>();
		Set<String> parsedPaths = new HashSet<String>();
		for (String location: locations) {
			SimpleEntry<String, SpringXmlParser> kv = null;
			if (location.startsWith("classpath")) {
				String tokens[] = location.split(":");
				kv = buildSpringXmlParser(resourcePaths, tokens[1]);
			} else {
				kv = buildSpringXmlParser(webappPaths, location);
			}
			if (kv.getValue() != null) {
				parseXmlFile(kv.getKey(), kv.getValue(), parsedPaths);
			}
		}
	}
	
	private interface PropertyGetter {
		String getProperty(SpringXmlParser parser);
	}
	
	private String getDbDriverProperty(PropertyGetter getter) {
		String tmp = null;
		for (SpringXmlParser parser : springXmlParsers) {
			tmp = getter.getProperty(parser);
			if (tmp != null) {
				break;
			}
		}
		if (PropertyUtil.isPlaceholder(tmp)) {
			String key = PropertyUtil.valueOfPlaceholder(tmp);
			return valuedProperties.containsKey(key) ? valuedProperties.get(key) : tmp;
		}
		return tmp;
	}

	public Set<String> getPropertiesFilePaths() {
		return propertiesFileLocations;
	}
	
	/**
	 * Get the database's url configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be parsed among properties files.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabaseUrl() {
		return getDbDriverProperty(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbUrl();
			}
		});
	}
	
	public String getDatabaseUsername() {
		return getDbDriverProperty(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbUsername();
			}
		});
	}
	
	public String getDatabasePassword() {
		return getDbDriverProperty(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbPassword();
			}
		});
	}
	
	public String getDatabaseDriverClassName() {
		return getDbDriverProperty(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbDriverClass();
			}
		});
	}
}