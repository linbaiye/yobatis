package org.nalby.yobatis.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.nalby.yobatis.util.TestUtil;

public class SpringXmlParserTests {
	private final static String[] DATASOURCE_CLASSES = {"org.apache.commons.dbcp.BasicDataSource", "com.alibaba.druid.pool.DruidDataSource"};

	@Test(expected = DocumentException.class)
	public void testWithInvalidXml() throws DocumentException, IOException {
		new SpringXmlParser(new ByteArrayInputStream("<bean></bean>".getBytes()));
	}
	
	@Test
	public void testNoDatasouce() throws DocumentException, IOException {
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream("<beans><bean class=\"org.test.Clazz\" /></beans>".getBytes()));
		assertTrue(null == parser.getDbUsername());
		parser =  new SpringXmlParser(new ByteArrayInputStream("<beans:beans xmlns:beans=\"http://test.com/beans\"></beans:beans>".getBytes()));
		assertTrue(null == parser.getDbUsername());
	}
	
	@Test
	public void testNoUsername() throws DocumentException, IOException {
		String xml = "<beans><bean class=\"org.apache.commons.dbcp.BasicDataSource\" /></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(null == parser.getDbUsername());
	}
	
	@Test
	public void testPNamespaceUsername() throws DocumentException, IOException {
		for (String clazz: DATASOURCE_CLASSES) {
			String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"" +
					clazz + "\" p:username=\"test\"/></beans>";
			SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
			assertTrue("test".equals(parser.getDbUsername()));
		}
	}
	
	@Test
	public void testProperyUsername() throws DocumentException, IOException {
		for (String clazz: DATASOURCE_CLASSES) {
			String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"" +
			clazz + "\"><property name=\"username\" value=\"test\"/></bean></beans>";
			SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
			assertTrue("test".equals(parser.getDbUsername()));
		}
	}
	
	@Test
	public void testImported() throws DocumentException, IOException { 
		String xml = "<beans><import resource=\"classpath:test.config\"/><import resource=\"test.config\"/></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> importedConfigFiles = parser.getImportedLocations();
		TestUtil.assertCollectionSizeAndStringsIn(importedConfigFiles, 2, "test.config",
				"classpath:test.config");
	}
	
	@Test
	public void testNoProperties() throws DocumentException, IOException {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
        "<property name=\"ignoreResourceNotFound\" value=\"true\" /></bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getPropertiesFileLocations();
		assertTrue(locations.isEmpty());
	}

	@Test
	public void testProperties() throws DocumentException, IOException {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
        "<property name=\"locations\"><list><value>classpath:conf/important.properties</value></list></property></bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getPropertiesFileLocations();
		assertTrue(locations.size() == 1);
		for (String location: locations) {
			assertTrue(location.equals("classpath:conf/important.properties"));
		}
	}
	
	@Test
	public void absolutePropertiesPath() throws DocumentException, IOException {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
        "<property name=\"locations\"><list><value>/important.properties</value></list></property></bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getPropertiesFileLocations();
		TestUtil.assertCollectionSizeAndStringsIn(locations, 1, "/important.properties");
	}
	
	@Test
	public void trimPropertiesValue() throws DocumentException, IOException {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
        "<property name=\"locations\"><list><value>  classpath:conf/important.properties   </value></list></property></bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getPropertiesFileLocations();
		assertTrue(locations.size() == 1);
		for (String location: locations) {
			assertTrue(location.equals("classpath:conf/important.properties"));
		}
	}

	@Test
	public void testMultipleProperties() throws DocumentException, IOException {
		String xml = "<beans><bean id=\"propertyConfigurer\" " +
        "class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
        "<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
        "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
        "<property name=\"locations\"><list><value>classpath:conf/important.properties</value><value>classpath:conf/test.properties</value></list></property></bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getPropertiesFileLocations();
		assertTrue(locations.size() == 2);
		for (String location: locations) {
			assertTrue(location.equals("classpath:conf/important.properties")
					|| location.equals("classpath:conf/test.properties"));
		}
	}

	@Test
	public void testContextPlaceholderTag() throws DocumentException, IOException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
				"<context:property-placeholder location=\"classpath:conf/test.properties, classpath:conf/important.properties\"/>" +
				"</beans>\n";
		SpringXmlParser parser = new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getPropertiesFileLocations();
		assertTrue(locations.size() == 2);
		for (String location : locations) {
			assertTrue(location.equals("classpath:conf/important.properties")
					|| location.equals("classpath:conf/test.properties"));
		}
	}
	
	@Test
	public void testMixedProperties() throws DocumentException, IOException {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
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
				"<context:property-placeholder location=\"classpath:conf/test.properties, classpath:conf/important.properties\"/>" +
				"<bean id=\"propertyConfigurer\" class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">" +
				   	"<property name=\"systemPropertiesModeName\" value=\"SYSTEM_PROPERTIES_MODE_OVERRIDE\" />" + 
				    "<property name=\"ignoreResourceNotFound\" value=\"true\" />" +
				    "<property name=\"locations\"><list><value>classpath:conf/important.properties</value></list></property></bean> "+
				"</beans>\n";
		SpringXmlParser parser = new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getPropertiesFileLocations();
		assertTrue(locations.size() == 2);
		for (String location : locations) {
			assertTrue(location.equals("classpath:conf/important.properties")
					|| location.equals("classpath:conf/test.properties"));
		}
	}
	
	@Test
	public void nondefaultDataSource() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"test.Datasource\"" +
					 " p:username=\"test\" p:password=\"username\" p:url=\"testurl\"/></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue("test".equals(parser.getDbUsername()));
		
		xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"test.Datasource\">"
		+ "<property name=\"username\" value=\"test\"/>"
		+ "<property name=\"password\" value=\"test\"/>"
		+ "<property name=\"url\" value=\"url\"/>"
		+ "<property name=\"driverClassName\" value=\"driver\"/>"
		+ "</bean></beans>";
		parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue("test".equals(parser.getDbUsername()));
		assertTrue("driver".equals(parser.getDbDriverClass()));
	}
	
	//Neither id nor class contains 'datasource'
	@Test
	public void beanContainsNoDatasource() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"test.NoClass\"" +
					 " p:username=\"test\" p:password=\"username\" p:url=\"testurl\"/></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertNull(parser.getDbPassword());
		assertNull(parser.getDbUrl());
	}
	
	@Test
	public void notEnoughProperties() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"test.Datasource\"" +
					 " p:username=\"test\" p:password=\"username\"/></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertNull(parser.getDbPassword());
		
		xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"test.Datasource\">"
				+ "<property name=\"username\" value=\"test\"/>"
				+ "<property name=\"password\" value=\"test\"/>"
				+ "</bean></beans>";
		parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertNull(parser.getDbPassword());
	}
	
	@Test
	public void placeholderConfiguerSingleLocation() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\"" +
				 " p:location=\"classpath:test.properties\" p:password=\"username\"></bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		TestUtil.assertCollectionEqual(parser.getPropertiesFileLocations(), "classpath:test.properties");
		
		xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">"
				+ "<property name=\"location\" value=\"classpath:test1.properties\"/>"
				+ "</bean></beans>";
		parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		TestUtil.assertCollectionEqual(parser.getPropertiesFileLocations(), "classpath:test1.properties");
		
		
		xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">"
				+ "<property name=\"location\"><value>classpath:test1.properties</value></property>"
				+ "</bean></beans>";
		parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		TestUtil.assertCollectionEqual(parser.getPropertiesFileLocations(), "classpath:test1.properties");
	}

	@Test
	public void placeholderConfiguerLocation() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">"
				+ "<property name=\"location\">"
				+ "<value>classpath:test1.properties</value>"
				+ "<value>classpath:test2.properties</value>"
				+ "</property>"
				+ "</bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getPropertiesFileLocations().isEmpty());
	}
	
	@Test
	public void singleValueInLocations() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">"
				+ "<property name=\"locations\">"
				+ "<value>classpath:test1.properties</value>"
				+ "</property>"
				+ "</bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		TestUtil.assertCollectionEqual(parser.getPropertiesFileLocations(), "classpath:test1.properties");
	}
	
	@Test
	public void setValuesInLocations() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">"
				+ "<property name=\"locations\">"
				+ "<set>"
				+ "<value>classpath:test1.properties</value>"
				+ "</set>"
				+ "</property>"
				+ "</bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		TestUtil.assertCollectionEqual(parser.getPropertiesFileLocations(), "classpath:test1.properties");
	}
	
	@Test
	public void emptyLocations() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">"
				+ "<property name=\"locations\">"
				+ "</property>"
				+ "</bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getPropertiesFileLocations().isEmpty());
	}
	
	@Test
	public void nostandardSpringConfigurer() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.omg.PlaceholderResolver\">"
				+ "<property name=\"locations\">"
				+ "<value>classpath:test1.properties</value>"
				+ "</property>"
				+ "</bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		TestUtil.assertCollectionEqual(parser.getPropertiesFileLocations(), "classpath:test1.properties");
		
		xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.omg.PlaceholderResolver\">"
				+ "<property name=\"locations\">"
				+ "<value>classpath:test1.prooep</value>"
				+ "</property>"
				+ "</bean></beans>";
		parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getPropertiesFileLocations().isEmpty());
	}
	
	@Test
	public void nostandardSpringLocations() throws DocumentException, IOException {
		String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\">"
				+ "<bean class=\"org.test.Resolver\">"
				+ "<property name=\"locations\">"
				+ "<list>"
				+ "<value>classpath:test1.properties</value>"
				+ "</list>"
				+ "</property>"
				+ "</bean></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		TestUtil.assertCollectionEqual(parser.getPropertiesFileLocations(), "classpath:test1.properties");
	}
	

}
