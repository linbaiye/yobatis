package org.nalby.yobatis.structure;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PomTreeTests {
	
	private Project project;
	
	private Folder webappFolder;
	
	@Before
	public void setup() {
		project = mock(Project.class);
		when(project.containsFile("pom.xml")).thenReturn(true);
		when(project.findFolder("src/main/webapp")).thenReturn(webappFolder);
	}
	
	@Test
	public void containerPom() {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"</project>\n";
		when(project.openFile("pom.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		PomTree tree = new PomTree(project);
		Pom pom = tree.getWarPom();
		assertTrue(pom == null);
		Set<Pom> poms = tree.getPoms();
		assertTrue(poms.size() == 1);
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
				"  <properties><hello>world</hello></properties>\n" +
				"</project>\n";
		when(project.openFile("pom.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		when(project.findFolder("src/main/resources")).thenReturn(null);
		PomTree tree = new PomTree(project);
		Pom pom = tree.getWarPom();
		Set<Folder> set = pom.getResourceFolders();
		assertTrue(set.size() == 0);
		assertTrue(pom.getWebappFolder() == null);
		assertTrue("worldworld".equals(pom.filterPlaceholders("${hello}${hello}")));
	}
	
	@Test
	public void with1Module() throws FileNotFoundException {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"  <properties><hello>world</hello></properties>\n" +
				"  <modules>\n" + 
				"    <module>hello</module>\n" + 
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
				"    <properties><next>test</next></properties>\n" +
				"    <build>\n" + 
				"      <resources>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/conf</directory>\n" + 
				"        </resource>\n" + 
				"      </resources>\n" + 
				"    </build>\n" + 
				"</project>"; 
		when(project.openFile("pom.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		Folder helloFolder = mock(Folder.class);
		when(helloFolder.openFile("pom.xml")).thenReturn(new ByteArrayInputStream(helloXml.getBytes()));
		when(project.findFolder("hello")).thenReturn(helloFolder);
		when(project.findFolder("missing")).thenReturn(null);
		when(helloFolder.findFolder("src/main/resources")).thenReturn(mock(Folder.class));
		when(helloFolder.findFolder("src/main/conf")).thenReturn(mock(Folder.class));
		when(helloFolder.findFolder("src/main/webapp")).thenReturn(mock(Folder.class));
		PomTree pomParser = new PomTree(project);
		Pom pom = pomParser.getWarPom();
		Set<Folder> resources = pom.getResourceFolders();
		assertTrue(resources.size() == 2);
		assertTrue(pom.getWebappFolder() != null);
		assertTrue("testworld${notfound}".equals(pom.filterPlaceholders("${next}${hello}${notfound}")));
	}

}
