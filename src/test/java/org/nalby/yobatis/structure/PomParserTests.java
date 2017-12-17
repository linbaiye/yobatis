package org.nalby.yobatis.structure;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.nalby.yobatis.util.TestUtil;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PomParserTests {
	
	private final static String PROJECT_PATH = "/yobatis";

	private final static String PROJECT_NAME = "yobatis";
	
	private Folder mockFolder(String name, boolean shouldContainPom) {
		Folder folder = mock(Folder.class);
		when(folder.name()).thenReturn(name);
		when(folder.path()).thenReturn(PROJECT_PATH + "/" + name);
		when(folder.containsFile("pom.xml")).thenReturn(shouldContainPom);
		return folder;
	}
	
	private Project mockProject(String xml) throws FileNotFoundException {
		Project project = mock(Project.class);
		when(project.path()).thenReturn(PROJECT_PATH);
		when(project.name()).thenReturn(PROJECT_NAME);
		when(project.containsFile("pom.xml")).thenReturn(true);
		when(project.getInputStream("pom.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		return project;
	}
	
	private String projectPath(String path) {
		if (path.startsWith("/")) {
			return PROJECT_PATH + path;
		}
		return PROJECT_PATH + "/" + path;
	}
	
	@Test
	public void singletonPom() throws FileNotFoundException {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <packaging>war</packaging>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"</project>\n";
		PomParser pomParser = new PomParser(mockProject(xml));
		Set<String> set = pomParser.getResourcePaths();
		assertTrue(set.size() == 1);
		TestUtil.assertStringsInCollection(set, PROJECT_PATH + "/src/main/resources");

		set = pomParser.getSourceCodePaths();
		assertTrue(set.size() == 1);
		TestUtil.assertStringsInCollection(set, PROJECT_PATH + "/src/main/java");

		set = pomParser.getWebappPaths();
		assertTrue(set.size() == 1);
		TestUtil.assertStringsInCollection(set, PROJECT_PATH + "/src/main/webapp");
	}
	
	@Test
	public void subModuleMissing() throws FileNotFoundException {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <packaging>war</packaging>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"  <modules>\n" + 
				"    <module>hello</module>\n" + 
				"    <module>missing</module>\n" + 
				"  </modules>\n" + 
				"</project>\n";
		String helloXml = "<project>\n" + 
				"    <modelVersion>4.0.0</modelVersion>\n" + 
				"    <groupId>test</groupId>\n" + 
				"    <artifactId>test</artifactId>\n" + 
				"    <version>1.3.0</version>\n" + 
				"    <packaging>war</packaging>\n" + 
				"    <name>test</name>\n" + 
				"    <url>test</url>\n" + 
				"    <build>\n" + 
				"      <resources>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/resources</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/conf</directory>\n" + 
				"        </resource>\n" + 
				"      </resources>\n" + 
				"    </build>\n" + 
				"</project>"; 
		Project project = mockProject(xml);
		List<Folder> folders = new ArrayList<Folder>();
		folders.add(mockFolder("hello", true));
		when(project.getSubFolders()).thenReturn(folders);
		when(project.getInputStream(projectPath("/hello/pom.xml")))
			.thenReturn(new ByteArrayInputStream(helloXml.getBytes()));
		PomParser pomParser = new PomParser(project);
		Set<String> set = pomParser.getResourcePaths();
		assertTrue(set.size() == 3);
		TestUtil.assertStringsInCollection(set, "/yobatis/src/main/resources", 
				"/yobatis/hello/src/main/resources", "/yobatis/hello/src/main/conf");
		
		
		set = pomParser.getSourceCodePaths();
		assertTrue(set.size() == 2);
		TestUtil.assertStringsInCollection(set, "/yobatis/src/main/java", 
				"/yobatis/hello/src/main/java");
	}

}
