package org.nalby.yobatis.structure;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.nalby.yobatis.util.TestUtil;

public class PomTreeTests {
	
	private Project project;
	
	private Folder webappFolder;
	
	private Folder resourceFolder;
	
	private Folder sourceCodeFolder;
	
	private List<Folder> sourceCodeFolders;
	
	private Folder dao;

	private Folder model;
	
	private File pomFile;
	
	private static final String DEFAULT_CODE_PATH = "/test/src/main/java";
	
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
		project = mock(Project.class);
		pomFile = mock(File.class);
		when(project.findFile("pom.xml")).thenReturn(pomFile);

		when(pomFile.open()).thenReturn(new ByteArrayInputStream(defaultXML.getBytes()));

		resourceFolder = mock(Folder.class);
		when(project.findFolder("src/main/resources")).thenReturn(resourceFolder);

		webappFolder = mock(Folder.class);
		when(project.findFolder("src/main/webapp")).thenReturn(webappFolder);

		sourceCodeFolder = mock(Folder.class);
		when(project.findFolder("src/main/java")).thenReturn(sourceCodeFolder);
		
		sourceCodeFolders = new LinkedList<>();
		
		dao = TestUtil.mockFolder("/test/src/main/java/dao");

		model = TestUtil.mockFolder("/test/src/main/java/model");

		sourceCodeFolders.add(dao);
		sourceCodeFolders.add(model);

		when(sourceCodeFolder.listFolders()).thenReturn(sourceCodeFolders);
		
		when(project.concatMavenRepositoryPath(anyString())).then((InvocationOnMock invocation) -> {
			String arg = (String) invocation.getArguments()[0];
			if (arg.contains("mysql-connector-java")) {
				return "/m2/" + arg;
			}
			return null;
		});
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
		when(pomFile.open()).thenReturn(new ByteArrayInputStream(xml.getBytes()));
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
		when(pomFile.open()).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		when(project.findFolder("src/main/resources")).thenReturn(null);
		when(project.findFolder("src/main/webapp")).thenReturn(null);
		PomTree tree = new PomTree(project);
		Pom pom = tree.getWarPom();
		Set<Folder> set = pom.getResourceFolders();
		assertTrue(set.size() == 0);
		assertTrue(pom.getWebappFolder() == null);
		assertTrue("worldworld".equals(pom.filterPlaceholders("${hello}${hello}")));

		assertTrue(null == pom.filterPlaceholders(null));
	}
	
	@Test
	public void with1Submodule() throws FileNotFoundException {
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
		when(pomFile.open()).thenReturn(new ByteArrayInputStream(xml.getBytes()));

		File subpom = mock(File.class);
		when(subpom.open()).thenReturn(new ByteArrayInputStream(helloXml.getBytes()));

		Folder helloFolder = mock(Folder.class);
		when(helloFolder.findFile("pom.xml")).thenReturn(subpom);

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
	
	@Test
	public void findDaoFolders() {
		PomTree tree = new PomTree(project);
		List<Folder> folders = tree.lookupDaoFolders();
		assertTrue(folders.size() == 1);
		assertTrue(dao == folders.get(0));
	}
	
	
	
	@Test
	public void findModelFolders() {
		PomTree tree = new PomTree(project);
		List<Folder> folders = tree.lookupModelFolders();
		assertTrue(folders.size() == 1);
		assertTrue(model == folders.get(0));

		folders = tree.lookupModelFolders();
		assertTrue(folders.size() == 1);
	}
	
	@Test
	public void findResourceFolders() {
		PomTree tree = new PomTree(project);
		List<Folder> folders = tree.lookupResourceFolders();
		assertTrue(folders.size() == 1);
		assertTrue(resourceFolder == folders.get(0));
	}
	
	@Test
	public void getSqlJar() {
		PomTree tree = new PomTree(project);
		assertTrue(tree.getDatabaseJarPath("com.mysql.jdbc.Driver").startsWith("/m2"));
	}
	
	@Test
	public void nullSqljar() {
		PomTree tree = new PomTree(project);
		assertTrue(tree.getDatabaseJarPath("com.jdbc.Driver") == null);
	}
	
	@Test
	public void findMatchingDaoFolder() {
		PomTree tree = new PomTree(project);
		assertTrue(tree.findMostMatchingDaoFolder(model) == dao);
	}
	
	@Test
	public void noDaoFolder() {
		sourceCodeFolders.remove(dao);
		PomTree tree = new PomTree(project);
		assertNull(tree.findMostMatchingDaoFolder(model));
	}
	
	@Test
	public void multipleDaoFolders() {
		Folder modelFolder = TestUtil.mockFolder(DEFAULT_CODE_PATH + "/user/model");
		sourceCodeFolders.add(modelFolder);
		PomTree tree = new PomTree(project);
		assertTrue(tree.findMostMatchingDaoFolder(modelFolder) == dao);
		Folder userdaoFolder = TestUtil.mockFolder(DEFAULT_CODE_PATH + "/user/dao");
		sourceCodeFolders.add(userdaoFolder);
		assertTrue(tree.findMostMatchingDaoFolder(modelFolder) == userdaoFolder);
	}
	
	@Test
	public void findResourceFolder() {
		PomTree tree = new PomTree(project);
		assertTrue(tree.findMostMatchingResourceFolder(model) == resourceFolder);
		Folder test = TestUtil.mockFolder("/test");
		assertTrue(tree.findMostMatchingResourceFolder(test) == resourceFolder);
	}
	
	@Test
	public void noResourceFolder() {
		when(project.findFolder("src/main/resources")).thenReturn(null);
		PomTree tree = new PomTree(project);
		assertNull(tree.findMostMatchingResourceFolder(model));
	}
	
}
