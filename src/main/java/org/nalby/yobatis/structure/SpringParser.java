package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.AntPathMatcher;
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
	
	private Set<Folder> resourceFolders;

	private Map<String, String> valuedProperties;
	
	private Folder webappFolder;
	
	private PomParser pomParser;

	private AntPathMatcher antPathMatcher;
	
	private Set<String> resourceFilepaths;
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	/**
	 * Construct a {@code SpringParser} that analyzes the spring files.
	 * @param project see {@link org.nalby.yobatis.structure.Project}
	 * @param pomParser 
	 * @param initParamValues The param-value in web.xml, including servlet and application
	 * context.
	 */
	public SpringParser(Project project, PomParser pomParser, Set<String> initParamValues) {
		Expect.notNull(project, "project must not be null.");
		Expect.notNull(pomParser, "pomParser must not be null.");
		Expect.notNull(initParamValues, "initParamValues must not be null.");
		Set<String> locations = parseLocationsInInitParamValues(initParamValues);
		if (locations.isEmpty()) {
			throw new UnsupportedProjectException("Failed to find spring config files.");
		}
		this.pomParser = pomParser;
		this.project = project;
		this.webappFolder = pomParser.getWebappFolder();
		this.resourceFolders = pomParser.getResourceFolders();
		antPathMatcher = new AntPathMatcher();
		buildResourcePathSet();
		parseSpringXmlFiles(locations);
	}
	
	/**
	 * Iterate all resource files' paths.
	 */
	private void buildResourcePathSet() {
		resourceFilepaths = new HashSet<String>();
		resourceFilepaths.addAll(webappFolder.getAllFilepaths());
		for (Folder folder : resourceFolders) {
			resourceFilepaths.addAll(folder.getAllFilepaths());
		}
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
						|| "".equals(tokens[i+2])) {
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
				value = pomParser.filterPlaceholders(value.trim());
				valuedProperties.put(name, value);
			}
		} catch (Exception e) {
			logger.info("Failed to load properties file:{}.", path);
		} finally {
			project.closeInputStream(inputStream);
		}
	}
	
	private final static String CLASSPATH_PATTERN = "classpath\\*?:.*";
	/**
	 * Concatenate locations configured in &lt;import&gt; and placeholder beans, locations
	 * start with 'file:' will just be ignored. The following rules apply:
	 * <ol>
	 * <li>
	 * If a location starts with 'classpath:', paths of all resource folders will concatenate 
	 * with it. (/xxx/src/main/resources, classpath:&#42;/test.xml -> /xxx/src/main/resources/&#42;/test.xml")
	 * </li>
	 * <li>
	 * If a location starts with '/' and is configured in a placeholder bean, we concatenate it
	 * with webapp folder's path. (/xx/src/main/webapp, /test.properties -> /xx/src/main/webapp/test.properties)
	 * </li>
	 * <li>
	 * Otherwise we just concatenate it with {@code basepath}.
	 * </li>
	 * </ol>
	 * @param basepath the folder path of the file that contains the locations.
	 * @param locations locations to concatenate.
	 * @return the concatenated paths.
	 */
	private Set<String> locationsToAntPatterns(String basepath, Set<String> locations, 
			boolean fromPlaceholderBean) {
		Set<String> result = new HashSet<String>();
		for (String val: locations) {
			val = val.replaceAll("[\\s+]", "");
			String location = this.pomParser.filterPlaceholders(val);
			if (location.startsWith("file:")) {
				logger.info("Could not process file:{}.", location);
				continue;
			}
			if (Pattern.matches(CLASSPATH_PATTERN, location)) {
				String tokens[] = location.split(":");
				for (Folder folder : resourceFolders) {
					String tmp = FolderUtil.concatPath(folder.path(), tokens[1]);
					result.add(tmp);
				}
			} else if (location.startsWith("/") && fromPlaceholderBean) {
				String tmp = FolderUtil.concatPath(webappFolder.path(), location);
				result.add(tmp);
			} else {
				String tmp = FolderUtil.concatPath(basepath, location);
				result.add(tmp);
			}
		}
		return result;
	}
	
	private Set<String> locationsToActualPaths(String basepath, Set<String> locations,
			boolean fromPlaceholderBean) {
		Set<String> antpaths = locationsToAntPatterns(basepath, locations, fromPlaceholderBean);
		Set<String> result = new HashSet<String>();
		for (String antpath: antpaths) {
			if (!AntPathMatcher.isPattern(antpath)) {
				result.add(antpath);
				continue;
			}
			for (String path: resourceFilepaths) {
				if (antPathMatcher.match(antpath, path)) {
					result.add(path);
				}
			}
		}
		return result;
	}

	private void parserPropertiesFiles(String thisXmlPath, SpringXmlParser xmlParser,
			Set<String> parsedPropertiesPaths) {
		Set<String> locations = xmlParser.getPropertiesFileLocations();
		String basepath = FolderUtil.folderPath(thisXmlPath);
		Set<String> paths = locationsToActualPaths(basepath, locations, true);
		for (String path: paths) {
			if (!parsedPropertiesPaths.contains(path)) {
				logger.info("Scanning properties file:{}.", path);
				parsedPropertiesPaths.add(path);
				addProperties(path);
			}
		}
	}

	private void parseXmlFile(Set<String> filepaths, Set<String> parsedSpringPaths, 
			Set<String> parsedPropertiesPaths) {
		for (String filepath: filepaths) {
			if (parsedSpringPaths.contains(filepath)) {
				continue;
			}
			parsedSpringPaths.add(filepath);
			SpringXmlParser springXmlParser = buildSpringXmlParser(filepath);
			if (springXmlParser == null) {
				continue;
			}
			logger.info("Scanning spring file:{}.", filepath);
			springXmlParsers.add(springXmlParser);
			parserPropertiesFiles(filepath, springXmlParser, parsedPropertiesPaths);
			String basepath = FolderUtil.folderPath(filepath);
			Set<String> newPaths = locationsToActualPaths(basepath, 
					springXmlParser.getImportedLocations(), false);
			parseXmlFile(newPaths, parsedSpringPaths, parsedPropertiesPaths);
		}
	}

	private void parseSpringXmlFiles(Set<String> locations) {
		springXmlParsers = new LinkedList<SpringXmlParser>();
		valuedProperties = new HashMap<String, String>();
		Set<String> parsedSrpingPaths = new HashSet<String>();
		Set<String> parsedPropertiesPaths = new HashSet<String>();
		Set<String> possiblePaths = locationsToActualPaths(webappFolder.path(), locations, false);
		parseXmlFile(possiblePaths, parsedSrpingPaths, parsedPropertiesPaths);
	}
	
	private interface PropertyGetter {
		String getProperty(SpringXmlParser parser);
	}
	
	private String getPropertyValue(PropertyGetter getter) {
		String tmp = null;
		for (SpringXmlParser parser : springXmlParsers) {
			tmp = getter.getProperty(parser);
			if (tmp != null) {
				break;
			}
		}
		List<String> placeholders = PropertyUtil.placeholdersFrom(tmp);
		for (String placeholder: placeholders) {
			String key = PropertyUtil.valueOfPlaceholder(placeholder);
			String val = valuedProperties.get(key);
			if (val != null) {
				tmp = tmp.replace(placeholder, val);
			}
		}
		return tmp;
	}

	/**
	 * Get the database's url configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be parsed among properties files.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabaseUrl() {
		return getPropertyValue(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbUrl();
			}
		});
	}
	
	public String getDatabaseUsername() {
		return getPropertyValue(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbUsername();
			}
		});
	}
	
	public String getDatabasePassword() {
		return getPropertyValue(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbPassword();
			}
		});
	}
	
	public String getDatabaseDriverClassName() {
		return getPropertyValue(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbDriverClass();
			}
		});
	}
}