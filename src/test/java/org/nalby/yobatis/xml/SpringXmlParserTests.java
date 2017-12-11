package org.nalby.yobatis.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.nalby.yobatis.exception.UnsupportedProjectException;

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
		String xml = "<beans><import resource=\"classpath:test.config\"/></beans>";
		SpringXmlParser parser =  new SpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		List<String> importedConfigFiles = parser.getImportedConfigFiles();
		assertTrue(importedConfigFiles.size() == 1 && importedConfigFiles.get(0).equals("classpath:test.config"));
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
}
