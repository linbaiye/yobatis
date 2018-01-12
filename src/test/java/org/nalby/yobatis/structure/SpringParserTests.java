package org.nalby.yobatis.structure;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.UnsupportedProjectException;

public class SpringParserTests {
	
	private SpringAntPathFileManager fileManager;
	
	private Set<String> locations;
	
	private Set<File> entryFiles;
	
	private Set<File> propertiesFiles;
	
	private SpringParser springParser;
	
	@Before
	public void setup() {
		fileManager = mock(SpringAntPathFileManager.class);
		locations = new HashSet<>();
		entryFiles = new HashSet<>();
		propertiesFiles = new HashSet<>();
	}
	
	
	@Test
	public void singlePomSpring() throws IOException {
		locations.add("test.xml");
		File file = mock(File.class);
		entryFiles.add(file);
		when(fileManager.findSpringFiles("test.xml")).thenReturn(entryFiles);
		when(fileManager.lookupDbProperty(file, "username")).thenReturn("username");
		when(fileManager.lookupDbProperty(file, "password")).thenReturn("${password}");
		when(fileManager.lookupDbProperty(file, "driverClassName")).thenReturn("className");
		when(fileManager.lookupDbProperty(file, "url")).thenReturn("url");
		
		Properties properties = new Properties();
		File propertyFile = mock(File.class);
		propertiesFiles.add(propertyFile);
		properties.load(new ByteArrayInputStream("password=123".getBytes()));
		when(fileManager.findPropertiesFiles(file)).thenReturn(propertiesFiles);
		when(fileManager.readProperties(propertyFile)).thenReturn(properties);

		springParser = new SpringParser(fileManager, locations);
		assertTrue(springParser.getDatabaseUsername().equals("username"));
		assertTrue(springParser.getDatabasePassword().equals("123"));
		assertTrue(springParser.getDatabaseUrl().equals("url"));
		assertTrue(springParser.getDatabaseDriverClassName().equals("className"));
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void badClasspath() {
		try {
			locations.add("classpath:");
			new SpringParser(fileManager, locations);
		} catch (UnsupportedProjectException e) {
			//expected.
		}
		locations.add("classpath*:");
		new SpringParser(fileManager, locations);
	}

}
