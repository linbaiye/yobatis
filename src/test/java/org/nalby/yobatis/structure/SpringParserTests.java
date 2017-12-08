package org.nalby.yobatis.structure;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.structure.Project.FolderSelector;

@RunWith(MockitoJUnitRunner.class)
public class SpringParserTests {
	
	@Mock
	private Project project;
	
	/**
	 * Mock folder
	 * @param name the folder name.
	 * @param path the folder path.
	 * @param containsFile the filename, with which when the containsFile is invoked 
	 * 	will return true.
	 * @return
	 */
	private Folder mockFolder(String name, String path, String containsFile) {
		Folder folder = mock(Folder.class);
		when(folder.containsFile(containsFile)).thenReturn(true);
		when(folder.path()).thenReturn(path);
		when(folder.name()).thenReturn(name);
		return folder;
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void testUnsupportedFilePath() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + 
				"	xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" + 
				"	xmlns:mybatis=\"http://mybatis.org/schema/mybatis-spring\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://www.springframework.org/schema/context/spring-context.xsd\n" + 
				"       		http://www.springframework.org/schema/aop \n" + 
				"       		http://www.springframework.org/schema/aop/spring-aop.xsd\n" + 
				"       		http://www.springframework.org/schema/mvc\n" + 
				"       		http://www.springframework.org/schema/mvc/spring-mvc.xsd\n" + 
				"       		http://mybatis.org/schema/mybatis-spring\n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"propertyConfigurer\"\n" + 
				"		class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">\n" + 
				"		<property name=\"locations\">\n" + 
				"			<list>\n" + 
				"				<value>s3.properties</value>\n" + 
				"			</list>\n" + 
				"		</property>\n" + 
				"	</bean>\n" + 
				"</beans>";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(springConfig.getBytes());
		when(project.getInputStream("/hello/test/test.conf")).thenReturn(inputStream);
		Folder mockedFolder = mockFolder("test", "/hello/test", "test.conf");
		List<Folder> list = Arrays.asList(mockedFolder);
		when(project.findFolders(any(FolderSelector.class))).thenReturn(list);
		List<String> tmp = Arrays.asList("classpath:test.conf");
		new SpringParser(project, tmp).getPropertiesFilePath();
	}

	@Test(expected = UnsupportedProjectException.class)
	public void testSingleSpringConfig() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + 
				"	xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" + 
				"	xmlns:mybatis=\"http://mybatis.org/schema/mybatis-spring\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://www.springframework.org/schema/context/spring-context.xsd\n" + 
				"       		http://www.springframework.org/schema/aop \n" + 
				"       		http://www.springframework.org/schema/aop/spring-aop.xsd\n" + 
				"       		http://www.springframework.org/schema/mvc\n" + 
				"       		http://www.springframework.org/schema/mvc/spring-mvc.xsd\n" + 
				"       		http://mybatis.org/schema/mybatis-spring\n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"propertyConfigurer\"\n" + 
				"		class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">\n" + 
				"		<property name=\"locations\">\n" + 
				"			<list>\n" + 
				"				<value>classpath:s3.properties</value>\n" + 
				"			</list>\n" + 
				"		</property>\n" + 
				"	</bean>\n" + 
				"\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"\n" + 
				"	<bean id=\"sqlTemplate\" class=\"org.mybatis.spring.SqlSessionTemplate\">\n" + 
				"		<constructor-arg index=\"0\" ref=\"sessionFactory\" />\n" + 
				"	</bean>\n" + 
				"\n" + 
				"	<mvc:annotation-driven />\n" + 
				"	<context:component-scan base-package=\"learn\" />\n" + 
				"	<mybatis:scan base-package=\"dao\" />\n" + 
				"</beans>";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(springConfig.getBytes());
		when(project.getInputStream("/hello/test/test.conf")).thenReturn(inputStream);
		Folder mockedFolder = mockFolder("test", "/hello/test", "test.conf");
		List<Folder> list = Arrays.asList(mockedFolder);
		when(project.findFolders(any(FolderSelector.class))).thenReturn(list);
		List<String> tmp = Arrays.asList("classpath:test.conf");
		SpringParser springParser = new SpringParser(project, tmp);
		assertTrue(springParser.getPropertiesFilePath().equals("/hello/test/s3.properties"));
		//Throws exception now;
		springParser.getDatabaseUsername();
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void testInvalidImportedFilePath() throws FileNotFoundException  {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + 
				"	xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" + 
				"	xmlns:mybatis=\"http://mybatis.org/schema/mybatis-spring\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://www.springframework.org/schema/context/spring-context.xsd\n" + 
				"       		http://www.springframework.org/schema/aop \n" + 
				"       		http://www.springframework.org/schema/aop/spring-aop.xsd\n" + 
				"       		http://www.springframework.org/schema/mvc\n" + 
				"       		http://www.springframework.org/schema/mvc/spring-mvc.xsd\n" + 
				"       		http://mybatis.org/schema/mybatis-spring\n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"  <import resource=\"config1.xml\"/>\n" + 
				"</beans>\n" + 
				"";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(springConfig.getBytes());
		when(project.getInputStream("/test/spring.conf")).thenReturn(inputStream);
		Folder mockedFolder = mockFolder("test", "/test", "spring.conf");
		List<Folder> list = Arrays.asList(mockedFolder);
		when(project.findFolders(any(FolderSelector.class))).thenReturn(list);
		List<String> tmp = Arrays.asList("classpath:spring.conf");
		//Throws exception now;
		new SpringParser(project, tmp);
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void testNonexsistentConfigFile() throws FileNotFoundException {
		String entryConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + 
				"	xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" + 
				"	xmlns:mybatis=\"http://mybatis.org/schema/mybatis-spring\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://www.springframework.org/schema/context/spring-context.xsd\n" + 
				"       		http://www.springframework.org/schema/aop \n" + 
				"       		http://www.springframework.org/schema/aop/spring-aop.xsd\n" + 
				"       		http://www.springframework.org/schema/mvc\n" + 
				"       		http://www.springframework.org/schema/mvc/spring-mvc.xsd\n" + 
				"       		http://mybatis.org/schema/mybatis-spring\n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"  <import resource=\"classpath:config1.xml\"/>\n" + 
				"</beans>";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(entryConfig.getBytes());
		when(project.getInputStream("/test/spring.xml")).thenReturn(inputStream);
		Folder mockedFolder = mockFolder("test", "/test", "spring.xml");
		List<Folder> list = Arrays.asList(mockedFolder);
		when(project.findFoldersContainingFile("spring.xml")).thenReturn(list);

		when(project.findFoldersContainingFile("config1.xml")).thenReturn(new LinkedList<Folder>());
		List<String> entryList = Arrays.asList("classpath:spring.xml");
		
		//Throws exception now;
		new SpringParser(project, entryList);
		
	}
	
	@Test
	public void testMultipleFiles() throws FileNotFoundException {
		String entryConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + 
				"	xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" + 
				"	xmlns:mybatis=\"http://mybatis.org/schema/mybatis-spring\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://www.springframework.org/schema/context/spring-context.xsd\n" + 
				"       		http://www.springframework.org/schema/aop \n" + 
				"       		http://www.springframework.org/schema/aop/spring-aop.xsd\n" + 
				"       		http://www.springframework.org/schema/mvc\n" + 
				"       		http://www.springframework.org/schema/mvc/spring-mvc.xsd\n" + 
				"       		http://mybatis.org/schema/mybatis-spring\n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"  <import resource=\"classpath:config1.xml\"/>\n" + 
				"</beans>";
		String config1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + 
				"	xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" + 
				"	xmlns:mybatis=\"http://mybatis.org/schema/mybatis-spring\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://www.springframework.org/schema/context/spring-context.xsd\n" + 
				"       		http://www.springframework.org/schema/aop \n" + 
				"       		http://www.springframework.org/schema/aop/spring-aop.xsd\n" + 
				"       		http://www.springframework.org/schema/mvc\n" + 
				"       		http://www.springframework.org/schema/mvc/spring-mvc.xsd\n" + 
				"       		http://mybatis.org/schema/mybatis-spring\n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"username\" value=\"mybatis\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"	<bean id=\"propertyConfigurer\"\n" + 
				"		class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">\n" + 
				"		<property name=\"locations\">\n" + 
				"			<list>\n" + 
				"				<value>classpath:s3.properties</value>\n" + 
				"			</list>\n" + 
				"		</property>\n" + 
				"	</bean>\n" + 
				"</beans>\n" + 
				"";
		ByteArrayInputStream inputStream = new ByteArrayInputStream(entryConfig.getBytes());
		when(project.getInputStream("/test" + Project.WEBAPP_PATH_SUFFIX + "/spring.xml")).thenReturn(inputStream);
		Folder mockedFolder = mockFolder("test", "/test" + Project.WEBAPP_PATH_SUFFIX, "spring.xml");
		when(project.findFoldersContainingFile(Project.WEBAPP_PATH_SUFFIX + "/spring.xml")).thenReturn(Arrays.asList(mockedFolder));

		mockedFolder = mockFolder("node1", "/test" + Project.WEBAPP_PATH_SUFFIX + "/node1", "config1.xml");
		when(project.getInputStream("/test/node1" + Project.WEBAPP_PATH_SUFFIX + "/config1.xml")).thenReturn(new ByteArrayInputStream(config1.getBytes()));
		when(project.findFoldersContainingFile("config1.xml")).thenReturn(new LinkedList<Folder>(Arrays.asList(mockedFolder)));

		mockedFolder = mockFolder("resources", "/src/main/resources", "s3.properties");
		when(project.findFoldersContainingFile("s3.properties")).thenReturn(new LinkedList<Folder>(Arrays.asList(mockedFolder)));

		List<String> entryList = Arrays.asList("WEB-INF/spring.xml");
		SpringParser parser = new SpringParser(project, entryList);
		assertTrue(parser.getDatabasePassword().equals("mybatis"));
		assertTrue(parser.getPropertiesFilePath().equals("/src/main/resources/s3.properties"));
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void invalidInitParam() {
		String value = "      test\n" + 
				"      path\n" + 
				"      classpath :" + 
				"";
		List<String> list = Arrays.asList(value);
		new SpringParser(project, list);
	}
	
	@Test
	public void singleClasspathParam() throws FileNotFoundException {
		String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + 
				"	xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" + 
				"	xmlns:mybatis=\"http://mybatis.org/schema/mybatis-spring\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://www.springframework.org/schema/context/spring-context.xsd\n" + 
				"       		http://www.springframework.org/schema/aop \n" + 
				"       		http://www.springframework.org/schema/aop/spring-aop.xsd\n" + 
				"       		http://www.springframework.org/schema/mvc\n" + 
				"       		http://www.springframework.org/schema/mvc/spring-mvc.xsd\n" + 
				"       		http://mybatis.org/schema/mybatis-spring\n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"username\" value=\"mybatis\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"	<bean id=\"propertyConfigurer\"\n" + 
				"		class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">\n" + 
				"		<property name=\"locations\">\n" + 
				"			<list>\n" + 
				"				<value>classpath:s3.properties</value>\n" + 
				"			</list>\n" + 
				"		</property>\n" + 
				"	</bean>\n" + 
				"</beans>\n" + 
				"";
		String[] paths = new String[]{"classpath :  test.xml", 
				"classpath:test.xml",
				"classpath:xx/xx/test.xml"};
		for (String path : paths) {
			String[] tokens = path.split(":");
			String filepath = tokens[1].trim();
			String folderpath = "/test/" + Project.MAVEN_RESOURCES_PATH;
			if (filepath.indexOf("/") != -1) {
				folderpath = "/test/" + Project.MAVEN_RESOURCES_PATH 
				+ "/" + filepath.replaceFirst("(^.*)/[^/]+$", "$1");
			}

			List<String> list = Arrays.asList(path);
			project = mock(Project.class);

			Folder mockedFolder = mockFolder("resources", folderpath, "test.xml");
			when(project.findFoldersContainingFile(Project.MAVEN_RESOURCES_PATH + "/" + filepath))
			.thenReturn(Arrays.asList(mockedFolder));

			ByteArrayInputStream inputStream = new ByteArrayInputStream(config.getBytes());
			when(project.getInputStream(folderpath + "/test.xml"))
			.thenReturn(inputStream);

			SpringParser parser = new SpringParser(project, list);

			mockedFolder = mockFolder("resources", "/test/"+ Project.MAVEN_RESOURCES_PATH, "s3.properties");
			when(project.findFoldersContainingFile(Project.MAVEN_RESOURCES_PATH+ "/s3.properties"))
			.thenReturn(Arrays.asList(mockedFolder));
			assertTrue(("/test/" + Project.MAVEN_RESOURCES_PATH + "/s3.properties")
			.equals(parser.getPropertiesFilePath()));
		}
	}
	
	@Test
	public void singleWebinfPathParam() throws FileNotFoundException {
		String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + 
				"	xmlns:mvc=\"http://www.springframework.org/schema/mvc\"\n" + 
				"	xmlns:mybatis=\"http://mybatis.org/schema/mybatis-spring\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://www.springframework.org/schema/context/spring-context.xsd\n" + 
				"       		http://www.springframework.org/schema/aop \n" + 
				"       		http://www.springframework.org/schema/aop/spring-aop.xsd\n" + 
				"       		http://www.springframework.org/schema/mvc\n" + 
				"       		http://www.springframework.org/schema/mvc/spring-mvc.xsd\n" + 
				"       		http://mybatis.org/schema/mybatis-spring\n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"com.mysql.jdbc.Driver\"/>\n" + 
				"        <property name=\"url\" value=\"jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8\"/>\n" + 
				"        <property name=\"username\" value=\"mybatis\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"	<bean id=\"propertyConfigurer\"\n" + 
				"		class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">\n" + 
				"		<property name=\"locations\">\n" + 
				"			<list>\n" + 
				"				<value>classpath:s3.properties</value>\n" + 
				"			</list>\n" + 
				"		</property>\n" + 
				"	</bean>\n" + 
				"</beans>\n" + 
				"";
		String[] paths = new String[]{"test.xml", 
				"WEB-INF/test.xml",
				"WEB-INF/test/test.xml"};
		for (String path : paths) {
			List<String> list = Arrays.asList(path);
			project = mock(Project.class);
			String folderpath = "/test/" + Project.WEBAPP_PATH_SUFFIX;

			if (path.indexOf("/") != -1) {
				folderpath = "/test/" + Project.WEBAPP_PATH_SUFFIX
				+ "/" + path.replaceFirst("(^.*)/[^/]+$", "$1");
			}

			Folder mockedFolder = mockFolder("webapp", folderpath, "test.xml");
			when(project.findFoldersContainingFile(Project.WEBAPP_PATH_SUFFIX + "/" + path))
			.thenReturn(Arrays.asList(mockedFolder));
			ByteArrayInputStream inputStream = new ByteArrayInputStream(config.getBytes());
			when(project.getInputStream("/test/" + Project.WEBAPP_PATH_SUFFIX + "/" + path))
			.thenReturn(inputStream);
			SpringParser parser = new SpringParser(project, list);
			mockedFolder = mockFolder("resources", "/test/"+ Project.MAVEN_RESOURCES_PATH, "s3.properties");
			when(project.findFoldersContainingFile(Project.MAVEN_RESOURCES_PATH+ "/s3.properties"))
			.thenReturn(Arrays.asList(mockedFolder));
			assertTrue(("/test/" + Project.MAVEN_RESOURCES_PATH+ "/s3.properties")
					.equals(parser.getPropertiesFilePath()));
		}
	}
	

}
