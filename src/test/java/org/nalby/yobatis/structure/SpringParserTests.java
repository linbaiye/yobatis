package org.nalby.yobatis.structure;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;
import org.nalby.yobatis.util.TestUtil;

@RunWith(MockitoJUnitRunner.class)
public class SpringParserTests {
	
	@Mock
	private Project project;
	
	@Before
	public void remockProject() {
		project = mock(Project.class);
	}
	
	@Test
	public void noResourceXmlExist() throws FileNotFoundException {
		String webappxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
		Set<String> initParamValues = TestUtil.buildStringSet("classpath:/test.conf");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/resources");
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		when(project.getInputStream("/yobatis/src/main/resources/test.conf")).thenThrow(new FileNotFoundException("test"));
		when(project.getInputStream("/yobatis/src/main/webapp/test.conf")).thenReturn(new ByteArrayInputStream(webappxml.getBytes()));
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword() == null);

	}


	@Test
	public void singletonResourceXml() throws FileNotFoundException {
		String springConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"	<bean id=\"dataSource\" class=\"org.apache.commons.dbcp.BasicDataSource\" destroy-method=\"close\">\n" + 
				"        <property name=\"driverClassName\" value=\"mybatis\"/>\n" + 
				"        <property name=\"url\" value=\"mybatis\"/>\n" + 
				"        <property name=\"password\" value=\"mybatis\"/>\n" + 
				"        <property name=\"username\" value=\"mybatis\"/>\n" + 
				"  </bean>\n" + 
				"<context:property-placeholder location=\"classpath:conf/test.properties, classpath:conf/important.properties\"/>" +
				"<bean id=\"propertyConfigurer\" class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
				   	"<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
				    "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
				    "<property name=\"locations\"><list><value>classpath:conf/important.properties</value></list></property>" +
			    "</bean>" +
				"</beans>";

		Set<String> initParamValues = TestUtil.buildStringSet("classpath:/test.conf");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/resources");
		Set<String> webappPaths = new HashSet<String>();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(springConfig.getBytes());
		when(project.getInputStream(argThat(IsNot.not("/yobatis/src/main/resources/test.conf")))).thenThrow(new FileNotFoundException());
		when(project.getInputStream("/yobatis/src/main/resources/test.conf")).thenReturn(inputStream);
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
		assertTrue(springParser.getDatabaseUrl().equals("mybatis"));
		assertTrue(springParser.getDatabaseUsername().equals("mybatis"));
		assertTrue(springParser.getDatabaseDriverClassName().equals("mybatis"));

		//Prefix '/' should make no difference.
		initParamValues = TestUtil.buildStringSet("classpath:test.conf");
		inputStream = new ByteArrayInputStream(springConfig.getBytes());
		when(project.getInputStream("/yobatis/src/main/resources/test.conf")).thenReturn(inputStream);
		springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
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
		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = new HashSet<String>();
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(springConfig.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));

		initParamValues = TestUtil.buildStringSet("/test.xml");
		inputStream = new ByteArrayInputStream(springConfig.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	
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
		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = new HashSet<String>();
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		when(project.getInputStream("/yobatis/src/main/webapp/conf/hello.xml")).thenReturn(new ByteArrayInputStream(importedXml.getBytes()));
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	

	@Test
	public void seperatedXmlFiles() throws FileNotFoundException {
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
		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/conf");
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		when(project.getInputStream("/yobatis/src/main/conf/conf/hello.xml")).thenReturn(new ByteArrayInputStream(importedXml.getBytes()));
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	
	
	
	@Test
	public void importXmlWithClasspathPrefix() throws FileNotFoundException {
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
		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/conf");
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		when(project.getInputStream("/yobatis/src/main/conf/conf/hello.xml")).thenReturn(new ByteArrayInputStream(importedXml.getBytes()));
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}

	@Test
	public void directImportLoop() throws FileNotFoundException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
				"  <import resource=\"test.xml\"/>\n" + 
				"</beans>";
		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/conf");
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}

	@Test
	public void indirectImportLoop() throws FileNotFoundException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
				"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
				"	xmlns:context=\"http://www.springframework.org/schema/context\"\n" + 
				"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans \n" + 
				"       		http://www.springframework.org/schema/beans/spring-beans.xsd \n" + 
				"       		http://www.springframework.org/schema/context \n" + 
				"       		http://mybatis.org/schema/mybatis-spring.xsd\">\n" + 
				"  <import resource=\"import.test.xml\"/>\n" + 
				"</beans>";
		String importedxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
				"  <import resource=\"test.xml\"/>\n" + 
				"</beans>";

		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/conf");
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		when(project.getInputStream("/yobatis/src/main/webapp/import.test.xml")).thenReturn(new ByteArrayInputStream(importedxml.getBytes()));
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue(springParser.getDatabasePassword().equals("mybatis"));
	}
	
	@Test
	public void propertiesFileInTheSameDir() throws FileNotFoundException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
				"        <property name=\"password\" value=\"${jdbc.password}\"/>\n" + 
				"        <property name=\"username\" value=\"${jdbc.username}\"/>\n" + 
				"  </bean>\n" + 
				"<context:property-placeholder location=\"conf/test.properties, classpath:conf/important.properties\"/>" +
				"</beans>";
		String propertiesContent = "jdbc.username= test";
		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/conf");
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		when(project.getInputStream("/yobatis/src/main/webapp/conf/test.properties")).thenReturn(new ByteArrayInputStream(propertiesContent.getBytes()));
		when(project.getInputStream("/yobatis/src/main/conf/conf/important.properties")).thenThrow(new FileNotFoundException());
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue("test".equals(springParser.getDatabaseUsername()));
		assertTrue("${jdbc.password}".equals(springParser.getDatabasePassword()));
	}
	
	@Test
	public void propertiesFileWithClasspathPrefix() throws FileNotFoundException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
				"        <property name=\"password\" value=\"${jdbc.password}\"/>\n" + 
				"        <property name=\"username\" value=\"${jdbc.username}\"/>\n" + 
				"  </bean>\n" + 
				"<context:property-placeholder location=\"conf/test.properties, classpath:conf/important.properties\"/>" +
				"</beans>";
		String propertiesContent = "jdbc.username= test";
		String importantProperteis = "jdbc.password= passowrd";
		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/conf");
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		when(project.getInputStream("/yobatis/src/main/webapp/conf/test.properties")).thenReturn(new ByteArrayInputStream(propertiesContent.getBytes()));
		when(project.getInputStream("/yobatis/src/main/conf/conf/important.properties")).thenReturn(new ByteArrayInputStream(importantProperteis.getBytes()));
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue("test".equals(springParser.getDatabaseUsername()));
		assertTrue("passowrd".equals(springParser.getDatabasePassword()));
		assertTrue("jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8".equals(springParser.getDatabaseUrl()));
		assertTrue("com.mysql.jdbc.Driver".equals(springParser.getDatabaseDriverClassName()));
	}
	
	@Test
	public void unconventionalPropertiesFilename() throws FileNotFoundException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
				"        <property name=\"password\" value=\"${jdbc.password}\"/>\n" + 
				"        <property name=\"username\" value=\"${jdbc.username}\"/>\n" + 
				"  </bean>\n" + 
				"<context:property-placeholder location=\"test\"/>" +
				"</beans>";
		String propertiesContent = "jdbc.username= test";
		Set<String> initParamValues = TestUtil.buildStringSet("test.xml");
		Set<String> resourcePaths = TestUtil.buildStringSet("/yobatis/src/main/conf");
		Set<String> webappPaths = TestUtil.buildStringSet("/yobatis/src/main/webapp");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		when(project.getInputStream("/yobatis/src/main/webapp/test.xml")).thenReturn(inputStream);
		when(project.getInputStream("/yobatis/src/main/webapp/test")).thenReturn(new ByteArrayInputStream(propertiesContent.getBytes()));
		SpringParser springParser = new SpringParser(project, resourcePaths, webappPaths, initParamValues);
		assertTrue("test".equals(springParser.getDatabaseUsername()));
		assertTrue("${jdbc.password}".equals(springParser.getDatabasePassword()));
		assertTrue("jdbc:mysql://localhost:3306/mybatis?characterEncoding=utf-8".equals(springParser.getDatabaseUrl()));
		assertTrue("com.mysql.jdbc.Driver".equals(springParser.getDatabaseDriverClassName()));
	}
	
	
}
