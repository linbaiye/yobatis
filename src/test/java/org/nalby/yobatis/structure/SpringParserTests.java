package org.nalby.yobatis.structure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.UnsupportedProjectException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class SpringParserTests {
	
	private OldSpringAntPatternFileManager fileManager;
	
	private Set<String> locations;
	
	private Set<String> entryFiles;
	
	private Set<String> propertiesFiles;
	
	private SpringParser springParser;
	
	@Before
	public void setup() {
		fileManager = mock(OldSpringAntPatternFileManager.class);
		locations = new HashSet<>();
		entryFiles = new HashSet<>();
		propertiesFiles = new HashSet<>();
	}
	
	@Test
	public void singlePomSpring() throws IOException {
		locations.add("test.xml");
		entryFiles.add("/test/test.xml");
		when(fileManager.findSpringFiles("test.xml")).thenReturn(entryFiles);
		when(fileManager.lookupPropertyOfSpringFile("/test/test.xml", "username")).thenReturn("username");
		when(fileManager.lookupPropertyOfSpringFile("/test/test.xml", "password")).thenReturn("${password}");
		when(fileManager.lookupPropertyOfSpringFile("/test/test.xml", "driverClassName")).thenReturn("className");
		when(fileManager.lookupPropertyOfSpringFile("/test/test.xml", "url")).thenReturn("url");
		
		Properties properties = new Properties();
		
		propertiesFiles.add("/test/test.properties");
		properties.load(new ByteArrayInputStream("password=123".getBytes()));

		when(fileManager.findPropertiesFiles("/test/test.xml")).thenReturn(propertiesFiles);
		when(fileManager.readProperties("/test/test.properties")).thenReturn(properties);
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
