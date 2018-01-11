package org.nalby.yobatis.structure;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OldPomTreeTests {
	
	private OldProject project;
	
	private OldFolder webappFolder;
	
	private OldFolder resourceFolder;
	
	private OldFolder sourceCodeFolder;
	
	private Set<OldFolder> sourceCodeFolders;
	
	private OldFolder dao;

	private OldFolder model;
	
	private String defaultXML = "<project>\n" + 
			"  <modelVersion>4.0.0</modelVersion>\n" + 
			"  <groupId>test</groupId>\n" + 
			"  <artifactId>test</artifactId>\n" + 
			"  <packaging>war</packaging>\n" + 
			"  <version>1.3.0</version>\n" + 
			"  <name>test</name>\n" + 
			"  <url>test</url>\n" + 
			"  <properties><hello>world</hello></properties>\n" +
			"  <dependencies>\n" +
			"  <dependency>\n" +
			"	<groupId>mysql</groupId>\n" + 
			"	<artifactId>mysql-connector-java</artifactId>\n" +
			"	<version>1</version>\n" +
			" </dependency>\n" +
			"  </dependencies>\n" +
			"</project>\n";

	@Before
	public void setup() {
		project = mock(OldProject.class);
		when(project.containsFile("pom.xml")).thenReturn(true);
		when(project.openFile("pom.xml")).thenReturn(new ByteArrayInputStream(defaultXML.getBytes()));

		resourceFolder = mock(OldFolder.class);
		when(project.findFolder("src/main/resources")).thenReturn(resourceFolder);

		webappFolder = mock(OldFolder.class);
		when(project.findFolder("src/main/webapp")).thenReturn(webappFolder);

		sourceCodeFolder = mock(OldFolder.class);
		when(project.findFolder("src/main/java")).thenReturn(sourceCodeFolder);
		
		sourceCodeFolders = new HashSet<>();
		
		dao = mock(OldFolder.class);
		when(dao.path()).thenReturn("/test/src/main/java/dao");

		model = mock(OldFolder.class);
		when(model.path()).thenReturn("/test/src/main/java/model");

		sourceCodeFolders.add(dao);
		sourceCodeFolders.add(model);
		
		when(project.concatMavenResitoryPath(anyString())).then(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String arg = (String)invocation.getArguments()[0];
				if (arg.contains("mysql-connector-java")) {
					return "/m2/" + arg;
				}
				return null;
			}
		});

		when(sourceCodeFolder.getAllFolders()).thenReturn(sourceCodeFolders);
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
		OldPomTree tree = new OldPomTree(project);
		OldPom pom = tree.getWarPom();
		assertTrue(pom == null);
		Set<OldPom> poms = tree.getPoms();
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
		when(project.findFolder("src/main/webapp")).thenReturn(null);
		OldPomTree tree = new OldPomTree(project);
		OldPom pom = tree.getWarPom();
		Set<OldFolder> set = pom.getResourceFolders();
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
		OldFolder helloFolder = mock(OldFolder.class);
		when(helloFolder.openFile("pom.xml")).thenReturn(new ByteArrayInputStream(helloXml.getBytes()));
		when(project.findFolder("hello")).thenReturn(helloFolder);
		when(project.findFolder("missing")).thenReturn(null);
		when(helloFolder.findFolder("src/main/resources")).thenReturn(mock(OldFolder.class));
		when(helloFolder.findFolder("src/main/conf")).thenReturn(mock(OldFolder.class));
		when(helloFolder.findFolder("src/main/webapp")).thenReturn(mock(OldFolder.class));
		OldPomTree pomParser = new OldPomTree(project);
		OldPom pom = pomParser.getWarPom();
		Set<OldFolder> resources = pom.getResourceFolders();
		assertTrue(resources.size() == 2);
		assertTrue(pom.getWebappFolder() != null);
		assertTrue("testworld${notfound}".equals(pom.filterPlaceholders("${next}${hello}${notfound}")));
	}
	
	@Test
	public void findDaoFolders() {
		OldPomTree tree = new OldPomTree(project);
		List<OldFolder> folders = tree.lookupDaoFolders();
		assertTrue(folders.size() == 1);
		assertTrue(dao == folders.get(0));
	}
	
	
	
	@Test
	public void findModelFolders() {
		OldPomTree tree = new OldPomTree(project);
		List<OldFolder> folders = tree.lookupModelFolders();
		assertTrue(folders.size() == 1);
		assertTrue(model == folders.get(0));

		folders = tree.lookupModelFolders();
		assertTrue(folders.size() == 1);
	}
	
	@Test
	public void findResourceFolders() {
		OldPomTree tree = new OldPomTree(project);
		List<OldFolder> folders = tree.lookupResourceFolders();
		assertTrue(folders.size() == 1);
		assertTrue(resourceFolder == folders.get(0));
	}
	
	@Test
	public void getSqlJar() {
		OldPomTree tree = new OldPomTree(project);
		assertTrue(tree.getDatabaseJarPath("com.mysql.jdbc.Driver").startsWith("/m2"));
	}
	
	@Test
	public void nullSqljar() {
		OldPomTree tree = new OldPomTree(project);
		assertTrue(tree.getDatabaseJarPath("com.jdbc.Driver") == null);
	}
	
}
