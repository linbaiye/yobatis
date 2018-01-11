package org.nalby.yobatis.structure;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TestUtil;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


public class OldSpringAntPatternFileManagerTests {
	
	private OldSpringAntPatternFileManager fileManager;
	
	private OldPomTree pomTree;
	
	private OldPom webpom;
	
	/**
	 * The webapp folder of webpm.
	 */
	private OldFolder webappFolder;

	/**
	 * Resource folders of webpom.
	 */
	private Set<OldFolder> resourceFolders;
	
	private OldProject project;
	
	private Set<OldPom> poms;
	
	/**
	 * used to add files to a folder.
	 */
	private Map<OldFolder, Set<String>> folderFiles;
	
	
	private OldPom mockPom() {
		OldPom pom = mock(OldPom.class);
		poms.add(pom);
		return pom;
	}
	
	
	@Before
	public void setup() {
		
		pomTree = mock(OldPomTree.class);
		poms = new HashSet<>();
		when(pomTree.getPoms()).thenReturn(poms);
		
		/* Setup webpom. */
		webpom = mockPom();
		when(webpom.filterPlaceholders(anyString())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return (String)invocation.getArguments()[0];
			}
		});
		webappFolder = TestUtil.mockOldFolder("/yobatis/src/main/webapp");
		when(webpom.getWebappFolder()).thenReturn(webappFolder);
		resourceFolders = new HashSet<>();
		when(webpom.getResourceFolders()).thenReturn(resourceFolders);

		when(pomTree.getWarPom()).thenReturn(webpom);
		

		project = mock(OldProject.class);

		fileManager = new OldSpringAntPatternFileManager(pomTree, project);
		
		folderFiles = new HashMap<>();
	}
	
	private void addFileToFolder(OldFolder folder, String ... names) {
		Set<String> files = folderFiles.get(folder);
		if (files == null) {
			files = new HashSet<>();
			folderFiles.put(folder, files);
			when(folder.getAllFilepaths()).thenReturn(files);
		}
		for (String name: names) {
			files.add(FolderUtil.concatPath(folder.path(), name));
		}
	}

	@Test
	public void emptyDir() {
		assertTrue(fileManager.findSpringFiles("/test.xml").isEmpty());
	}
	
	@Test
	public void nullHint() {
		assertTrue(fileManager.findSpringFiles("").isEmpty());
	}
	
	@Test
	public void relativePath() {
		addFileToFolder(webappFolder, "conf.xml", "test.xml");
		Set<String> files = fileManager.findSpringFiles("test.xml");
		assertTrue(files.size() == 1);
		TestUtil.assertStringsInCollection(files, webappFolder.path() + "/test.xml");
	}
	
	@Test
	public void absolutePath() {
		addFileToFolder(webappFolder, "conf.xml", "test.xml");
		Set<String> files = fileManager.findSpringFiles("/test.xml");
		assertTrue(files.size() == 1);
		TestUtil.assertStringsInCollection(files, webappFolder.path() + "/test.xml");
	}
	
	@Test
	public void relativeClasspathWithEmtpyResource() {
		addFileToFolder(webappFolder, "conf.xml", "test.xml");
		Set<String> files = fileManager.findSpringFiles("classpath:test.xml");
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void relativeClasspath() {
		OldFolder folder = TestUtil.mockOldFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml", "test1.xml");
		Set<String> files = fileManager.findSpringFiles("classpath:test.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, "/yobatis/src/main/resources/test.xml");
	}
	
	@Test
	public void brokenClasspath() {
		OldFolder folder = TestUtil.mockOldFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml", "test1.xml");
		Set<String> files = fileManager.findSpringFiles("classpath:");
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void absoluteClasspath() {
		OldFolder folder = TestUtil.mockOldFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml", "test1.xml");
		Set<String> files = fileManager.findSpringFiles("classpath:/test.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, "/yobatis/src/main/resources/test.xml");
	}
	
	@Test
	public void relativeAntPatternClasspath() {
		OldFolder folder = TestUtil.mockOldFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml", "test1.xml");
		Set<String> files = fileManager.findSpringFiles("classpath:*.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 2, 
				"/yobatis/src/main/resources/test.xml", "/yobatis/src/main/resources/test1.xml");
		
		files = fileManager.findSpringFiles("classpath:te*.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 2, 
				"/yobatis/src/main/resources/test.xml", "/yobatis/src/main/resources/test1.xml");
	}
	
	
	@Test
	public void prefixWildcardClasspath() {
		OldFolder folder = TestUtil.mockOldFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml");

		folder = TestUtil.mockOldFolder("/yobatis/src/main/resources1");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test1.xml");

		Set<String> files = fileManager.findSpringFiles("classpath*:test1.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, 
				"/yobatis/src/main/resources1/test1.xml");
		
		files = fileManager.findSpringFiles("classpath*:test*.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 2, 
				"/yobatis/src/main/resources1/test1.xml", "/yobatis/src/main/resources/test.xml");
	}
	
	@Test
	public void brokenPrefixWildcardClasspath() {
		OldFolder folder = TestUtil.mockOldFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml");

		folder = TestUtil.mockOldFolder("/yobatis/src/main/resources1");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test1.xml");

		Set<String> files = fileManager.findSpringFiles("classpath*:");
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void getImportedFilesWithNoExistedPath() {
		Set<String> files = fileManager.findImportSpringXmlFiles("null");
		assertTrue(files.isEmpty());
	}
	
	//When the spring file is not valid.
	@Test
	public void getImportedFilesWithInvalidXmlFile() {
		addFileToFolder(webappFolder, "test.xml");
		Set<String> files = fileManager.findSpringFiles("test.xml");
		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream("invalid file".getBytes()));
		files = fileManager.findImportSpringXmlFiles(webappFolder.path() + "/test.xml");
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void importNonExistedRelativePath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"test1.xml\" />" +
				"</beans>";
		addFileToFolder(webappFolder, "test.xml");
		Set<String> files = fileManager.findSpringFiles("test.xml");
		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		files = fileManager.findImportSpringXmlFiles(webappFolder.path() + "/test.xml");
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void importDirectRelativePath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"test1.xml\" />" +
				"</beans>";
		addFileToFolder(webappFolder, "test1.xml", "test.xml");
		Set<String> files = fileManager.findSpringFiles("test.xml");
		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		files = fileManager.findImportSpringXmlFiles(webappFolder.path() + "/test.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, webappFolder.path() + "/test1.xml");
	}
	
	
	@Test
	public void importIndirectRelativePath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"conf/test1.xml\" />" +
				"</beans>";
		addFileToFolder(webappFolder, "conf/test1.xml", "test.xml");
		Set<String> files = fileManager.findSpringFiles("test.xml");
		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		files = fileManager.findImportSpringXmlFiles(webappFolder.path() + "/test.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, webappFolder.path() + "/conf/test1.xml");
	}
	
	
	@Test
	public void importClassRelativePath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"classpath:test1.xml\" />" +
				"</beans>";
		addFileToFolder(webappFolder, "conf/test1.xml", "test.xml");
		Set<String> files = fileManager.findSpringFiles("test.xml");

		OldFolder folder = TestUtil.mockOldFolder("/yobatis/src/main/resources");
		addFileToFolder(folder, "test1.xml", "test2.xml");
		resourceFolders.add(folder);

		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		files = fileManager.findImportSpringXmlFiles(webappFolder.path() + "/test.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, folder.path() + "/test1.xml");
	}
	
	@Test
	public void importWildcardClassPath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"classpath*:test1.xml\" />" +
				"</beans>";
		addFileToFolder(webappFolder, "conf/test1.xml", "test.xml");
		Set<String> files = fileManager.findSpringFiles("test.xml");

		OldPom pom = mockPom();
		OldFolder folder = TestUtil.mockOldFolder("/yobatis/submodule/src/main/resources");
		Set<OldFolder> resources = new HashSet<>();
		resources.add(folder);
		addFileToFolder(folder, "test1.xml", "test2.xml");
		when(pom.getResourceFolders()).thenReturn(resources);

		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		files = fileManager.findImportSpringXmlFiles(webappFolder.path() + "/test.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, folder.path() + "/test1.xml");
	}

	@Test
	public void findRelativePathProperties() {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
		        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
		        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
		        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
		        "<property name=\"locations\"><list><value>important.properties</value></list></property></bean></beans>";
		addFileToFolder(webappFolder, "important.properties", "test.xml");
		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		fileManager.findSpringFiles("test.xml");

		Set<String> files = fileManager.findPropertiesFiles(webappFolder.path() + "/test.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, webappFolder.path() + "/important.properties");
	}

	@Test
	public void findAbsolutePathProperties() {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
		        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
		        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
		        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
		        "<property name=\"locations\"><list><value>/important.properties</value></list></property></bean></beans>";
		OldFolder folder = TestUtil.mockOldFolder("/yobatis/src/main/resources");
		addFileToFolder(folder, "test.xml");
		resourceFolders.add(folder);
		
		addFileToFolder(webappFolder, "important.properties");

		when(project.openFile(folder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		fileManager.findSpringFiles("classpath:test.xml");

		Set<String> files = fileManager.findPropertiesFiles(folder.path() + "/test.xml");
		TestUtil.assertCollectionSizeAndStringsIn(files, 1, webappFolder.path() + "/important.properties");
	}
	
	@Test
	public void readProperties() {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
		        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
		        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
		        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
		        "<property name=\"locations\"><list><value>important.properties</value></list></property></bean></beans>";
		addFileToFolder(webappFolder, "important.properties", "test.xml");
		String propertiesFile = "key1=val1\nkey2=${help}\nkey3=";
		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));

		fileManager.findSpringFiles("test.xml");
		Set<String> files = fileManager.findPropertiesFiles(webappFolder.path() + "/test.xml");

		String file = files.iterator().next();
		when(project.openFile(webappFolder.path() + "/important.properties")).thenReturn(new ByteArrayInputStream(propertiesFile.getBytes()));

		when(webpom.filterPlaceholders(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String arg = (String)invocation.getArguments()[0];
				if ("${help}".equals(arg)) {
					return "val2";
				}
				return arg;
			}
		});
		Properties properties = fileManager.readProperties(file);
		assertTrue(properties.getProperty("key1").equals("val1"));
		assertTrue(properties.getProperty("key2").equals("val2"));
		assertTrue(properties.getProperty("key3") == null);
	}
	
	@Test
	public void readPropertiesWithInvalidPath() {
		assertTrue(fileManager.readProperties("test") == null);
	}

	@Test
	public void readPropertiesWithException() {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
		        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
		        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
		        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
		        "<property name=\"locations\"><list><value>important.properties</value></list></property></bean></beans>";
		addFileToFolder(webappFolder, "important.properties", "test.xml");
		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(xml.getBytes()));

		fileManager.findSpringFiles("test.xml");
		Set<String> files = fileManager.findPropertiesFiles(webappFolder.path() + "/test.xml");
		String file = files.iterator().next();
		when(project.openFile(webappFolder.path() + "/important.properties")).thenThrow(new IllegalArgumentException());
		fileManager.readProperties(file);
	}

	
	@Test
	public void readPropertyOfSpringFile() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"org.apache.commons.dbcp.BasicDataSource\">" +
				 "<property name=\"username\" value=\"test\"/><property name=\"driverClassName\" value=\"mysql\"/>"
				 + "<property name=\"url\" value=\"testurl\"/><property name=\"password\" value=\"${test}\"/></bean></beans>";
		addFileToFolder(webappFolder, "important.properties", "test.xml");
		when(project.openFile(webappFolder.path() + "/test.xml")).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		fileManager.findSpringFiles("test.xml");
		when(webpom.filterPlaceholders("${test}")).thenReturn("password");
		String name = fileManager.lookupPropertyOfSpringFile(webappFolder.path() + "/test.xml", "username");
		assertTrue(name.equals("test"));
		String password = fileManager.lookupPropertyOfSpringFile(webappFolder.path() + "/test.xml", "password");
		assertTrue(password.equals("password"));
		String url = fileManager.lookupPropertyOfSpringFile(webappFolder.path() + "/test.xml", "url");
		assertTrue(url.equals("testurl"));
		String driver = fileManager.lookupPropertyOfSpringFile(webappFolder.path() + "/test.xml", "driverClassName");
		assertTrue(driver.equals("mysql"));
	}
}
