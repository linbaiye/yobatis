package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.AntPathMatcher;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.xml.SpringXmlParser;
/**
 * Used to parse spring configuration files. The main purpose is to locate
 * database's properties by parsing imported spring xml files and properties
 * files. It's poorly designed for now and should be optimized in future.
 * 
 * @author Kyle Lin
 */
public class SpringParser {

	private List<SpringXmlParser> springXmlParsers;
	
	private Map<String, String> valuedProperties;
	
	private PomTree pomTree;

	private AntPathMatcher antPathMatcher;
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	/**
	 * Construct a {@code SpringParser} that analyzes the spring files.
	 * @param pomTree {@link PomTree}
	 * @param initParamValues The param-value in web.xml, including servlet and application
	 * context.
	 */
	public SpringParser(PomTree pomTree, Set<String> initParamValues) {
		Expect.notNull(pomTree, "pomParser must not be null.");
		Expect.notNull(initParamValues, "initParamValues must not be null.");
		Set<String> locations = parseLocationsInInitParamValues(initParamValues);
		if (locations.isEmpty()) {
			throw new UnsupportedProjectException("Failed to find spring config files.");
		}
		this.pomTree = pomTree;
		antPathMatcher = new AntPathMatcher();
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
				if ("classpath".equals(tokens[i]) || "classpath*".equals(tokens[i])) {
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
	
	/**
	 * A convenient class that helps analyze a spring xml file and a properties file,
	 * while the pom is used to solve placeholders, the folder is for searching files when importing
	 * a file with relative path happens.
	 */
	private class FilepathMetadata {
		/* The Pom that this filepath belongs to. */
		private Pom pom;
		/* The Folder that this filepath belongs to. */
		private Folder folder;

		private String filepath;
		
		public FilepathMetadata(Pom pom, Folder folder, String path) {
			this.pom = pom;
			this.folder = folder;
			this.filepath = path;
		}
		
		public Pom getPom() {
			return pom;
		}

		public Folder getFolder() {
			return folder;
		}

		public String getFilepath() {
			return filepath;
		}

		private InputStream getInputStream() {
			String name = FolderUtil.filename(filepath);
			return folder.openInputStream(name);
		}
		
		public SpringXmlParser getSpringXmlParser() {
			InputStream inputStream = null;
			try {
				inputStream = getInputStream();
				return new SpringXmlParser(inputStream);
			} catch (Exception e) {
				return null;
			} finally {
				FolderUtil.closeStream(inputStream);
			}
		}
		
		public void parseProperties() {
			try (InputStream inputStream = getInputStream()) {
				Properties properties = new Properties();
				properties.load(inputStream);
				Set<String> keys = properties.stringPropertyNames();
				for (String key: keys) {
					String value = properties.getProperty(key);
					if (value == null) {
						logger.info("Discard property {}.", key);
						continue;
					}
					value = pom.filterPlaceholders(value.trim());
					valuedProperties.put(key, value);
				}
			} catch (Exception e) {
				//Ignore.
			}
		}
	}
	
	
	/**
	 * Find the folder that contains directly the file of the path.
	 * @param folder the folder that contains the file directly or indirectly.
	 * @param path the file path.
	 */
	private Folder findFolderContainingFile(Folder folder, String path) {
		String filebase = FolderUtil.folderPath(path);
		if (filebase.equals(folder.path())) {
			return folder;
		}
		String diff = filebase.replace(folder.path() + "/", "");
		return folder.findFolder(diff);
	}
	

	/**
	 * Find files that match the ant path pattern in the folder.
	 * @param result
	 * @param pom the pom module the found file belongs to.
	 * @param folder the folder
	 * @param antPattern the ant path pattern.
	 */
	private void findFilesOfAntPatternInFolder(Set<FilepathMetadata> result, Pom pom, Folder folder, String antPattern) {
		String antpath = FolderUtil.concatPath(folder.path(), antPattern);
		for (String filepath : folder.getAllFilepaths()) {
			if (antpath.equals(filepath) || antPathMatcher.match(antpath, filepath)) {
				Folder targetDir = findFolderContainingFile(folder, filepath);
				if (targetDir != null) {
					result.add(new FilepathMetadata(pom, targetDir, filepath));
				}
			}
		}
	}
	
	/**
	 * Find files that starts with the pattern of 'classpath:xxx'.
	 * @param result
	 * @param pom the module to search in.
	 * @param antPattern the pattern of 'classpat:xxx'.
	 */
	private void findFilesOfClasspathPattern(Set<FilepathMetadata> result, Pom pom, String antPattern) {
		for (Folder resourceItem : pom.getResourceFolders()) {
			findFilesOfAntPatternInFolder(result, pom, resourceItem, antPattern);
		}
	}
	
	/**
	 * Find files that starts with the pattern of 'classpath*:xxx'.
	 * @param result
	 * @param antPattern the pattern of 'classpat*:xxx'.
	 */
	private void findFilesOfWildcardClasspathPattern(Set<FilepathMetadata> result, String antPattern) {
		for (Pom pom : pomTree.getPoms()) {
			findFilesOfClasspathPattern(result, pom, antPattern);
		}
	}
	
	/**
	 * Find files that are imported(by a spring xml file). Care needs to be taken when
	 * searching files start with '/', if the locations are imported as spring xml files,
	 * the '/' has no effect as the location will be treated as a relative path. However,
	 * if the locations are imported as properties files, the '/' means the root path of
	 * this project.
	 * @param locations
	 * @param pom
	 * @param folder
	 * @param propertiesLocation
	 * @return
	 */
	private Set<FilepathMetadata> findImportedFiles(Set<String> locations, Pom pom,
			Folder folder, boolean propertiesLocation) {
		Set<FilepathMetadata> result = new HashSet<FilepathMetadata>();
		for (String val: locations) {
			String location = pom.filterPlaceholders(val);
			if (location.startsWith("classpath:")) {
				String tokens[] = location.split(":");
				findFilesOfClasspathPattern(result, pom, tokens[1]);
			} else if (location.startsWith("classpath*:")) {
				String tokens[] = location.split(":");
				findFilesOfWildcardClasspathPattern(result, tokens[1]);
			} else {
				Pom tmpPom = pom;
				Folder tmpFolder = folder;
				if (location.startsWith("/") && propertiesLocation) {
					tmpFolder = null;
					tmpPom = pomTree.getWarPom();
					if (tmpPom != null) {
						tmpFolder = tmpPom.getWebappFolder();
					}
				}
				if (tmpPom == null || tmpFolder == null) {
					continue;
				}
				findFilesOfAntPatternInFolder(result, tmpPom, tmpFolder, location);
			}
		}
		return result;
	}

	private void iterateSpringXmlFiles(Set<FilepathMetadata> filepathMetadatas, 
			Set<String> parsedXmlFiles, Set<String> parsedPropertiesFiles) {
		for (FilepathMetadata metadata: filepathMetadatas) {
			if (parsedXmlFiles.contains(metadata.getFilepath())) {
				continue;
			}
			logger.info("Scanning file:{}.", metadata.getFilepath());
			parsedXmlFiles.add(metadata.getFilepath());
			SpringXmlParser parser = metadata.getSpringXmlParser();
			if (parser == null) {
				logger.info("File:{} is not a spring config file.", metadata.getFilepath());
				continue;
			}
			springXmlParsers.add(parser);
			Set<FilepathMetadata> propertiesFiles = findImportedFiles(parser.getPropertiesFileLocations(),
					metadata.getPom(), metadata.getFolder(), true);
			for (FilepathMetadata propertiesFile: propertiesFiles) {
				if (!parsedPropertiesFiles.contains(propertiesFile.getFilepath())) {
					logger.info("Scanning properties file:{}.", propertiesFile.getFilepath());
					parsedPropertiesFiles.add(propertiesFile.getFilepath());
					propertiesFile.parseProperties();
				}
			}
			Set<FilepathMetadata> newSpringFiles = findImportedFiles(parser.getImportedLocations(), metadata.getPom(), 
					metadata.getFolder(), false);
			iterateSpringXmlFiles(newSpringFiles, parsedXmlFiles, parsedPropertiesFiles);
		}
	}
	
	
	private void parseSpringXmlFiles(Set<String> locations) {
		Pom warPom = pomTree.getWarPom();
		if (null == warPom) {
			throw new UnsupportedProjectException("No war module found.");
		}
		springXmlParsers = new LinkedList<SpringXmlParser>();
		valuedProperties = new HashMap<String, String>();
		Set<String> parsedSrpingPaths = new HashSet<String>();
		Set<String> parsedPropertiesPaths = new HashSet<String>();
		Set<FilepathMetadata> paths = findImportedFiles(locations, warPom, 
				warPom.getWebappFolder(), false);
		iterateSpringXmlFiles(paths, parsedSrpingPaths, parsedPropertiesPaths);
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
	 * could not be found anywhere.
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
	
	/**
	 * Get the database's username configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be found anywhere.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabaseUsername() {
		return getPropertyValue(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbUsername();
			}
		});
	}
	
	/**
	 * Get the database's password configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be found anywhere.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabasePassword() {
		return getPropertyValue(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbPassword();
			}
		});
	}
	
	/**
	 * Get the database's driver class name configured among spring config files. The returned
	 * value would be a placeholder (for instance ${jdbc.url}) if the placeholder
	 * could not be found anywhere.
	 * @return The value if configured, null otherwise.
	 */
	public String getDatabaseDriverClassName() {
		return getPropertyValue(new PropertyGetter() {
			@Override
			public String getProperty(SpringXmlParser parser) {
				return parser.getDbDriverClass();
			}
		});
	}
}