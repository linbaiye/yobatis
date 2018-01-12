package org.nalby.yobatis.structure;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TestUtil;

public class SpringAntPathFileManagerTests {
	
	private SpringAntPathFileManager fileManager;
	
	private PomTree pomTree;
	
	private Pom webpom;
	
	/**
	 * The webapp folder of webpm.
	 */
	private Folder webappFolder;

	/**
	 * Resource folders of webpom.
	 */
	private Set<Folder> resourceFolders;
	

	private Set<Pom> poms;
	
	/**
	 * used to add files to a folder.
	 */
	private Map<Folder, List<File>> filesOfFolders;
	
	
	private Pom mockPom() {
		Pom pom = mock(Pom.class);
		poms.add(pom);
		return pom;
	}
	
	
	@Before
	public void setup() {
		filesOfFolders = new HashMap<>();
		pomTree = mock(PomTree.class);
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
		webappFolder = TestUtil.mockFolder("/yobatis/src/main/webapp");
		when(webpom.getWebappFolder()).thenReturn(webappFolder);

		List<File> list = new LinkedList<>();
		filesOfFolders.put(webappFolder, list);
		when(webappFolder.listFiles()).thenReturn(list);

		resourceFolders = new HashSet<>();

		when(webpom.getResourceFolders()).thenReturn(resourceFolders);

		when(pomTree.getWarPom()).thenReturn(webpom);
		
		fileManager = new SpringAntPathFileManager(pomTree);

	}
	
	private void addFileToFolder(Folder folder, String ... names) {
		List<File> files = filesOfFolders.get(folder);
		if (files == null) {
			files = new LinkedList<>();
			filesOfFolders.put(folder, files);
			when(folder.listFiles()).thenReturn(files);
		}
		for (String name: names) {
			File file = mock(File.class);
			when(file.parentFolder()).thenReturn(folder);
			String path = FolderUtil.concatPath(folder.path(), name);
			when(file.path()).thenReturn(path);
			when(file.name()).thenReturn(name);
			files.add(file);
		}
	}
	
	private Folder addFolderToFolder(Folder dst, String name) {
		Folder folder = mock(Folder.class);
		when(folder.name()).thenReturn(name);
		String path = FolderUtil.concatPath(dst.path(), name);
		when(folder.path()).thenReturn(path);
		when(dst.listFolders()).thenReturn(Arrays.asList(folder));
		return folder;
	}
	
	private File getFileOfFolder(Folder folder, String name) {
		List<File> files = filesOfFolders.get(folder);
		if (files != null) {
			for (File file : filesOfFolders.get(folder)) {
				if (name.equals(file.name())) {
					return file;
				}
			}
		}
		return null;
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
		Set<File> files = fileManager.findSpringFiles("test.xml");
		assertTrue(files.size() == 1);
		TestUtil.assertCollectionContains(filesOfFolders.get(webappFolder), files);
	}
	
	@Test
	public void absolutePath() {
		addFileToFolder(webappFolder, "conf.xml", "test.xml");
		Set<File> files = fileManager.findSpringFiles("/test.xml");
		assertTrue(files.size() == 1);
		TestUtil.assertCollectionContains(filesOfFolders.get(webappFolder), files);
	}
	
	@Test
	public void relativeClasspathWithEmtpyResource() {
		Set<File> files = fileManager.findSpringFiles("classpath:test.xml");
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void relativeClasspath() {
		Folder folder = TestUtil.mockFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml", "test1.xml");
		Set<File> files = fileManager.findSpringFiles("classpath:test.xml");
		assertTrue(files.size() == 1);
		TestUtil.assertCollectionContains(filesOfFolders.get(folder), files);
	}
	
	@Test
	public void brokenClasspath() {
		Folder folder = TestUtil.mockFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml", "test1.xml");
		Set<File> files = fileManager.findSpringFiles("classpath:");
		assertTrue(files.isEmpty());
	}
	
	
	@Test
	public void absoluteClasspath() {
		Folder folder = TestUtil.mockFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);
		addFileToFolder(folder, "test.xml", "test1.xml");
		Set<File> files = fileManager.findSpringFiles("classpath:/test.xml");
		assertTrue(files.size() == 1);
		TestUtil.assertCollectionContains(filesOfFolders.get(folder), files);
	}
	
	@Test
	public void relativeAntPatternClasspath() {
		Folder folder = TestUtil.mockFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);;
		addFileToFolder(folder, "test.xml", "test1.xml");
		Set<File> files = fileManager.findSpringFiles("classpath:*.xml");
		TestUtil.assertCollectionEqual(filesOfFolders.get(folder), files);
	}
	
	@Test
	public void prefixWildcardClasspath() {
		Folder folder = TestUtil.mockFolder("/yobatis/src/main/resources");
		resourceFolders.add(folder);;
		addFileToFolder(folder, "test.xml");
		
		//Multiple resource folders.
		Folder folder2 = TestUtil.mockFolder("/yobatis/src/main/resources1");
		resourceFolders.add(folder2);
		addFileToFolder(folder2, "test1.xml");

		Set<File> files = fileManager.findSpringFiles("classpath*:test1.xml");
		TestUtil.assertCollectionEqual(filesOfFolders.get(folder2), files);

		files = fileManager.findSpringFiles("classpath*:test*.xml");
		TestUtil.assertCollectionContains(files, filesOfFolders.get(folder2));
		TestUtil.assertCollectionContains(files, filesOfFolders.get(folder));
	}
	
	@Test
	public void getImportedFilesWithNoExistedPath() {
		Set<File> files = fileManager.findSpringFiles((File)null);
		assertTrue(files.isEmpty());
	}
	
	@Test
	public void getImportedFilesWhenFileExcpetion() {
		addFileToFolder(webappFolder, "test1.xml");
		File file = filesOfFolders.get(webappFolder).get(0);
		when(file.open()).thenThrow(new ResourceNotAvailableExeception("err"));
		fileManager.findSpringFiles("test1.xml");
		assertTrue(fileManager.findSpringFiles(file).isEmpty());
	}
	
	@Test
	public void importNonExistedRelativePath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"test1.xml\" />" +
				"</beans>";
		addFileToFolder(webappFolder, "test.xml");
		Set<File> files = fileManager.findSpringFiles("test.xml");
		File file = filesOfFolders.get(webappFolder).get(0);
		when(file.open()).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		files = fileManager.findSpringFiles(file);
		assertTrue(files.isEmpty());
	}
	
	//The <import> contains no folder path, such as <import resource="next-config.xml"/>.
	@Test
	public void importDirectRelativePath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"test1.xml\" />" +
				"</beans>";
		addFileToFolder(webappFolder, "test1.xml", "test.xml");
		File file = getFileOfFolder(webappFolder, "test.xml");
		Set<File> files = fileManager.findSpringFiles("test.xml");
		when(file.open()).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		files = fileManager.findSpringFiles(file);
		TestUtil.assertCollectionEqual(files, getFileOfFolder(webappFolder, "test1.xml"));
	}
	
	
	//Imitate the situation that <import> contains folder path, such as 
	//<import resource="dir1/dir2/next-config.xml"/>.
	@Test
	public void importIndirectRelativePath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"conf/test1.xml\" />" +
				"</beans>";
		addFileToFolder(webappFolder, "test.xml");
		Folder folder = addFolderToFolder(webappFolder, "conf");
		addFileToFolder(folder, "test1.xml");;
		
		File file = getFileOfFolder(webappFolder, "test.xml");
		when(file.open()).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		Set<File> files = fileManager.findSpringFiles("test.xml");
		files = fileManager.findSpringFiles(file);
		//Assert that the file under 'conf' folder was found.
		TestUtil.assertCollectionEqual(files, filesOfFolders.get(folder));
	}
	
	@Test
	public void importClassRelativePath() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">\n" +
				"<import resource=\"classpath:test1.xml\" />" +
				"</beans>";
		Folder resouceFolder = TestUtil.mockFolder("/yobatis/src/main/resource");
		resourceFolders.add(resouceFolder);
		addFileToFolder(resouceFolder, "test1.xml");

		addFileToFolder(webappFolder, "test.xml");
		File file = getFileOfFolder(webappFolder, "test.xml");
		when(file.open()).thenReturn(new ByteArrayInputStream(testXml.getBytes()));

		Set<File> files = fileManager.findSpringFiles("test.xml");
		files = fileManager.findSpringFiles(file);
		//Assert that the file under 'conf' folder was found.
		TestUtil.assertCollectionEqual(files, filesOfFolders.get(resouceFolder));
	}

	
	@Test
	public void findRelativePathProperties() {
		String testXml = "<beans><bean id=\"propertyConfigurer\" " +
		        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
		        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
		        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
		        "<property name=\"locations\"><list><value>important.properties</value></list></property></bean></beans>";
		addFileToFolder(webappFolder, "important.properties", "test.xml");
		File file = getFileOfFolder(webappFolder, "test.xml");
		when(file.open()).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		fileManager.findSpringFiles("test.xml");
		Set<File> files = fileManager.findPropertiesFiles(file);
		TestUtil.assertCollectionEqual(files, getFileOfFolder(webappFolder, "important.properties"));
	}


	//Properties files start with '/' should be searched under the webapp folder.
	@Test
	public void findAbsolutePathProperties() {
		String testXml = "<beans><bean id=\"propertyConfigurer\" " +
		        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
		        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
		        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
		        "<property name=\"locations\"><list><value>/important.properties</value></list></property></bean></beans>";

		Folder resouceFolder = TestUtil.mockFolder("/yobatis/src/main/resource");
		resourceFolders.add(resouceFolder);
		addFileToFolder(resouceFolder, "test.xml");
		File file = getFileOfFolder(resouceFolder, "test.xml");
		when(file.open()).thenReturn(new ByteArrayInputStream(testXml.getBytes()));

		fileManager.findSpringFiles("classpath:test.xml");
		
		addFileToFolder(webappFolder, "important.properties");

		Set<File> files = fileManager.findPropertiesFiles(file);
		TestUtil.assertCollectionEqual(files, getFileOfFolder(webappFolder, "important.properties"));
	}
	
	
	
	// Lookup database properties in the datasouce bean.
	@Test
	public void lookupDbProperty() {
		String testXml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"org.apache.commons.dbcp.BasicDataSource\">" +
				 "<property name=\"username\" value=\"test\"/><property name=\"driverClassName\" value=\"mysql\"/>"
				 + "<property name=\"url\" value=\"testurl\"/><property name=\"password\" value=\"${test}\"/></bean></beans>";
		addFileToFolder(webappFolder, "test.properties", "test.xml");
		File xmlFile = getFileOfFolder(webappFolder, "test.xml");
		when(xmlFile.open()).thenReturn(new ByteArrayInputStream(testXml.getBytes()));
		when(webpom.filterPlaceholders("${test}")).thenReturn("filteredpassword");
		Set<File> files = fileManager.findSpringFiles("test.xml");
		String name = fileManager.lookupDbProperty(files.iterator().next(), "username");
		assertTrue(name.equals("test"));
		String password = fileManager.lookupDbProperty(files.iterator().next(), "password");
		assertTrue(password.equals("filteredpassword"));
		String url = fileManager.lookupDbProperty(files.iterator().next(), "url");
		assertTrue(url.equals("testurl"));
		String driverClassName = fileManager.lookupDbProperty(files.iterator().next(), "driverClassName");
		assertTrue(driverClassName.equals("mysql"));
		assertNull(fileManager.lookupDbProperty(files.iterator().next(), "notexisted"));
	}
	

	@Test
	public void readProperties() {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
		        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
		        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
		        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
		        "<property name=\"locations\"><list><value>test.properties</value></list></property></bean></beans>";

		String propertiesFileContent = "key1=val1\nkey2=${help}\nkey3=";

		addFileToFolder(webappFolder, "test.properties", "test.xml");

		File xmlFile = getFileOfFolder(webappFolder, "test.xml");
		when(xmlFile.open()).thenReturn(new ByteArrayInputStream(xml.getBytes()));

		File propertiesFile = getFileOfFolder(webappFolder, "test.properties");
		when(propertiesFile.open()).thenReturn(new ByteArrayInputStream(propertiesFileContent.getBytes()));


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
		Set<File> files = fileManager.findSpringFiles("test.xml");

		files = fileManager.findPropertiesFiles(files.iterator().next());

		Properties properties = fileManager.readProperties(files.iterator().next());
		assertTrue(properties.getProperty("key1").equals("val1"));
		assertTrue(properties.getProperty("key2").equals("val2"));
		assertTrue(properties.getProperty("key3") == null);
	}
	
	//When the file is not managed by the manager.
	@Test
	public void exceptions() {
		assertNull(fileManager.lookupDbProperty((File)null, "test"));
		assertNull(fileManager.readProperties(null));
		

		String xml = "<beans><bean id=\"propertyConfigurer\" " +
		        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
		        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
		        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
		        "<property name=\"locations\"><list><value>test.properties</value></list></property></bean></beans>";
		addFileToFolder(webappFolder, "test.properties", "test.xml");
		File xmlFile = getFileOfFolder(webappFolder, "test.xml");
		when(xmlFile.open()).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		File propertiesFile = getFileOfFolder(webappFolder, "test.properties");
		when(propertiesFile.open()).thenThrow(new ResourceNotAvailableExeception("exception."));
		Set<File> files = fileManager.findSpringFiles("test.xml");
		files = fileManager.findPropertiesFiles(files.iterator().next());
		assertNull(fileManager.readProperties(files.iterator().next()));
		
	}
	
	
}
