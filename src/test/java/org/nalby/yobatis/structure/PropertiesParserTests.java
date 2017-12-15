package org.nalby.yobatis.structure;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import static org.mockito.Mockito.*;
public class PropertiesParserTests {
	@Test
	public void loadPropertiesFiles() throws FileNotFoundException {
		String testProperties = "hello= ${world}\n" + 
				"test=joy\n" + 
				"abc=";
		String test1Properties = "k1=v2";
		Set<String> locations = new HashSet<String>();
		locations.add("test.properties");
		locations.add("test1.properties");
		Project project = mock(Project.class);
		when(project.getInputStream("test.properties")).thenReturn(new ByteArrayInputStream(testProperties.getBytes()));
		when(project.getInputStream("test1.properties")).thenReturn(new ByteArrayInputStream(test1Properties.getBytes()));
		PropertiesParser parser = new PropertiesParser(project, locations);
		assertTrue("joy".equals(parser.getProperty("test")));
		assertTrue("".equals(parser.getProperty("abc")));
		//overwrote
		assertTrue("v2".equals(parser.getProperty("k1")));
		assertTrue("${world}".equals(parser.getProperty("hello")));
	}
}
