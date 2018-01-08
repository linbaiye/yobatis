package org.nalby.yobatis.structure;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class SpringParserTests {
	
	private PomTree pomTree;
	
	private Set<Folder> webPomResourceFolders;

	private Folder webappFolder;
	
	private Pom webPom;
	
	private Set<String> webappFolderFilepaths;
	
	private Set<Pom> poms;
	
	private Set<Folder> webappFolders;
	
	private final static String PROJECT_PATH = "/yobatis";

	private final static String DEFAULT_RESOURCE_PATH = PROJECT_PATH + "/src/main/resources";

	private final static String WEBAPP_PATH = "/yobatis/src/main/webapp";
	
	private final static String defaultSpringConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
			"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
			"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
			"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
			"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
			"       		http://www.springframework.org/schema/context \n" + 
			"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
			"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
			"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
			"        <property name=\"url\" value=\"mybatisurl\"/>\n" + 
			"        <property name=\"password\" value=\"${mybatis}\"/>\n" + 
			"        <property name=\"username\" value=\"username\"/>\n" + 
			"  </bean>\n" + 
			"  \n" + 
			" 	<bean id=\"propertyConfigurer\" class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
			" <property name=\"locations\">" +
            "<list><value>yobatis.properties</value></list>" +
            "</property></bean> " + 
			"</beans>";
	
	
	private final static String defaultProperties = "mybatis=password";


	private Folder mockFolder(String path) {
		Folder folder = mock(Folder.class);
		when(folder.path()).thenReturn(path);
		return folder;
	}
	
	
	private void setupWebpom() {
		webPom = mock(Pom.class);
		webPomResourceFolders = new HashSet<>();
		when(webPom.getResourceFolders()).thenReturn(webPomResourceFolders);

		webappFolder = mockFolder(WEBAPP_PATH);
		when(webappFolder.openFile("yobatis.xml")).thenReturn(new ByteArrayInputStream(defaultSpringConfig.getBytes()));
		when(webappFolder.openFile("yobatis.properties")).thenReturn(new ByteArrayInputStream(defaultProperties.getBytes()));

		webappFolders = new HashSet<>();
		when(webappFolder.getAllFolders()).thenReturn(webappFolders);

		webappFolderFilepaths = new HashSet<>();
		webappFolderFilepaths.add(webappFolder.path() + "/yobatis.xml");
		webappFolderFilepaths.add(webappFolder.path() + "/yobatis.properties");

		when(webappFolder.getAllFilepaths()).thenReturn(webappFolderFilepaths);

		when(webPom.getWebappFolder()).thenReturn(webappFolder);
		
		when(webPom.filterPlaceholders(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object arg = invocation.getArguments()[0];
				return (String)arg;
			}
		});
	}
	
	
	
	private void setupPomTree() {
		pomTree = mock(PomTree.class);
		when(pomTree.getWarPom()).thenReturn(webPom);

		poms = new HashSet<Pom>();
		poms.add(webPom);
		when(pomTree.getPoms()).thenReturn(poms);
	}
	
	
	@Before
	public void setup() {
		setupWebpom();
		setupPomTree();
	}
	
	private void addFileToWebappDir(String file) {
		if (file.startsWith("/")) {
			webappFolderFilepaths.add(WEBAPP_PATH + file);
		} else {
			webappFolderFilepaths.add(WEBAPP_PATH + "/" + file);
		}
	}
	
	
	private String getFilepath(Folder folder, String path) {
		if (path.contains(":")) {
			String[] tokens = path.split(":");
			return FolderUtil.concatPath(folder.path(), tokens[1].trim());
		} else {
			return FolderUtil.concatPath(folder.path(), path);
		}
	}
	
	private void addFilesToDir(Folder folder, @SuppressWarnings("unchecked") SimpleEntry<String, String> ... files) {
		Set<String> paths = new HashSet<String>();
		for (SimpleEntry<String, String> entry: files) {
			String filepath = getFilepath(folder, entry.getKey());
			paths.add(filepath);
			when(folder.openFile(FolderUtil.filename(filepath))).thenReturn(new ByteArrayInputStream(entry.getValue().getBytes()));
		}
		when(folder.getAllFilepaths()).thenReturn(paths);
	}

	private Set<String> wireXmlFile(String content, String name) {
		Set<String> initParamValues = TestUtil.buildSet(name);
		when(webPom.filterPlaceholders(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
		        return (String)invocation.getArguments()[0];
		    }
		});
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
		addFileToWebappDir(name);
		name = name.replaceFirst("^/", "");
		when(webappFolder.openFile(name)).thenReturn(inputStream);
		return initParamValues;
	}
	
	/*@Test
	public void noXmlFileExist() throws FileNotFoundException {
		Set<String> initParamValues = TestUtil.buildSet("test.conf");
		when(webPom.filterPlaceholders("test.conf")).thenReturn("test.conf");
		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword() == null);
	}

	@Test
	public void singletonWebappXml() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"</beans>";
		Set<String> initParamValues = wireXmlFile(springConfig, "test.xml");
		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	
	@Test
	public void singletonWebappAbsoluteXml() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"</beans>";
		Set<String> initParamValues = wireXmlFile(springConfig, "/test.xml");
		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void singletonWebappXmlWithFolderPath() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"</beans>";
		Folder folder = mockFolder("folder1/folder2");
		addFilesToDir(folder, new SimpleEntry<String, String>("test.xml", springConfig));
		Set<String> initParamValues = wireXmlFile(springConfig, "folder1/folder2/test.xml");
		when(webappFolder.findFolder("folder1/folder2")).thenReturn(folder);
		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void singletonClasspathXml() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"</beans>";
		Set<String> initParamValues = wireXmlFile(springConfig, "classpath:test.xml");

		Folder folder = mockFolder(DEFAULT_RESOURCE_PATH);
		addFilesToDir(folder, new SimpleEntry<String, String>("test.xml", springConfig));
		webPomResourceFolders.add(folder);

		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void singletonWildcardClasspathXml() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"</beans>";
		Set<String> initParamValues = wireXmlFile(springConfig, "classpath*:test.xml");

		Folder folder = mockFolder(DEFAULT_RESOURCE_PATH);
		addFilesToDir(folder, new SimpleEntry<String, String>("test.xml", springConfig));
		webPomResourceFolders.add(folder);

		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void singletonClasspathAbsoluteXml() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"</beans>";
		Set<String> initParamValues = wireXmlFile(springConfig, "classpath:/test.xml");

		Folder folder = mockFolder(DEFAULT_RESOURCE_PATH);
		addFilesToDir(folder, new SimpleEntry<String, String>("classpath:/test.xml", springConfig));
		webPomResourceFolders.add(folder);

		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void importXmlWithoutClasspathPrefix() throws FileNotFoundException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"  <import resource=\"conf/hello.xml\"/>\n" + 
				"</beans>";
		String importedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"</beans>";
		Set<String> initParamValues = wireXmlFile(xml, "test.xml");
		Folder folder = mockFolder(webappFolder.path() + "/conf");
		addFilesToDir(folder, new SimpleEntry<String, String>("hello.xml", importedXml));
		webappFolderFilepaths.add(WEBAPP_PATH + "/conf/hello.xml");
		when(webappFolder.findFolder("conf")).thenReturn(folder);

		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void importClasspath() throws FileNotFoundException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"  <import resource=\"classpath:conf/hello.xml\"/>\n" + 
				"</beans>";
		String importedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"</beans>";
		Set<String> initParamValues = wireXmlFile(xml, "test.xml");
		Folder folder = mockFolder(DEFAULT_RESOURCE_PATH);

		Folder confFolder = mockFolder(folder.path() + "/conf");
		addFilesToDir(confFolder, new SimpleEntry<String, String>("hello.xml", importedXml));

		when(folder.findFolder("conf")).thenReturn(confFolder);
		addFilesToDir(folder, new SimpleEntry<String, String>("conf/hello.xml", importedXml));
		webPomResourceFolders.add(folder);
		
		SpringParser springParser = new SpringParser(pomTree, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}*/
	
	@Test(expected = UnsupportedProjectException.class)
	public void invalidClasspath() {
		try {
			Set<String> set = TestUtil.buildSet("classpath:");
			new SpringParser(pomTree, set);
			fail();
		} catch (Exception e) {
			//Expected.
		}
		Set<String> set = TestUtil.buildSet("classpath*:");
		new SpringParser(pomTree, set);
	}

	@Test(expected = UnsupportedProjectException.class)
	public void nullConfigPath() {
		Set<String> set = TestUtil.buildSet("");
		new SpringParser(pomTree, set);
	}
	
	@Test
	public void webappSpringXml() {
		Set<String> set = TestUtil.buildSet("yobatis.xml");
		SpringParser parser = new SpringParser(pomTree, set);
		assertTrue("username".equals(parser.getDatabaseUsername()));
		assertTrue("password".equals(parser.getDatabasePassword()));
		assertTrue("com.mysql.jdbc.Driver".equals(parser.getDatabaseDriverClassName()));
		assertTrue("mybatisurl".equals(parser.getDatabaseUrl()));
	}
	
	@Test
	public void webappSpringXmlAbsolutePath() {
		String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"mybatisurl\"/>\n" + 
				"        <property name=\"password\" value=\"${mybatis}\"/>\n" + 
				"        <property name=\"username\" value=\"username\"/>\n" + 
				"  </bean>\n" + 
				"  \n" + 
				" 	<bean id=\"propertyConfigurer\" class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
				" <property name=\"locations\">" +
	            "<list><value>/yobatis.properties</value></list>" +
	            "</property></bean> " + 
				"</beans>";
		when(webappFolder.openFile("yobatis.xml")).thenReturn(new ByteArrayInputStream(config.getBytes()));
		Set<String> set = TestUtil.buildSet("/yobatis.xml");
		SpringParser parser = new SpringParser(pomTree, set);
		assertTrue("username".equals(parser.getDatabaseUsername()));
		assertTrue("password".equals(parser.getDatabasePassword()));
	}
	
	@Test
	public void classpathSpringXml() {
		Folder folder = mockFolder(DEFAULT_RESOURCE_PATH);
		when(folder.openFile("yobatis.xml")).thenReturn(new ByteArrayInputStream(defaultSpringConfig.getBytes()));
		when(folder.openFile("yobatis.properties")).thenReturn(new ByteArrayInputStream(defaultSpringConfig.getBytes()));
		webPomResourceFolders.add(folder);

		Set<String> set = TestUtil.buildSet("classpath:yobatis.xml");
		SpringParser parser = new SpringParser(pomTree, set);
		assertTrue("username".equals(parser.getDatabaseUsername()));
		assertTrue("password".equals(parser.getDatabasePassword()));
	}

	
}
