package org.nalby.yobatis.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.nalby.yobatis.exception.SqlConfigIncompleteException;

public class SpringSuperXmlParserTests {
	private final static String[] DATASOURCE_CLASSES = {"org.apache.commons.dbcp.BasicDataSource", "com.alibaba.druid.pool.DruidDataSource"};

	@Test(expected = DocumentException.class)
	public void testWithInvalidXml() throws DocumentException, IOException {
		new RootSpringXmlParser(new ByteArrayInputStream("<bean></bean>".getBytes()));
	}
	
	
	
	@Test(expected = SqlConfigIncompleteException.class)
	public void testNoDatasouce() throws DocumentException, IOException {
		RootSpringXmlParser parser =  new RootSpringXmlParser(new ByteArrayInputStream("<beans><bean class=\"org.test.Clazz\" /></beans>".getBytes()));
		try {
			parser.getDbUsername();
			fail();
		} catch (SqlConfigIncompleteException e) {
			//expected.
		}
		parser =  new RootSpringXmlParser(new ByteArrayInputStream("<beans:beans xmlns:beans=\"http://test.com/beans\"></beans:beans>".getBytes()));
		parser.getDbUsername();
	}
	
	@Test(expected = SqlConfigIncompleteException.class)
	public void testNoUsername() throws DocumentException, IOException {
		String xml = "<beans><bean class=\"org.apache.commons.dbcp.BasicDataSource\" /></beans>";
		RootSpringXmlParser parser =  new RootSpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		parser.getDbUsername();
	}
	
	@Test
	public void testPNamespaceUsername() throws DocumentException, IOException {
		for (String clazz: DATASOURCE_CLASSES) {
			String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"" +
					clazz + "\" p:username=\"test\"/></beans>";
			RootSpringXmlParser parser =  new RootSpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
			assertTrue("test".equals(parser.getDbUsername()));
		}
	}
	
	@Test
	public void testProperyUsername() throws DocumentException, IOException {
		for (String clazz: DATASOURCE_CLASSES) {
			String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"" +
			clazz + "\"><property name=\"username\" value=\"test\"/></bean></beans>";
			RootSpringXmlParser parser =  new RootSpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
			assertTrue("test".equals(parser.getDbUsername()));
		}
	}
	
	@Test
	public void testMultipleXmlSegements() throws DocumentException, IOException {
		String xml = "<beans><bean class=\"org.apache.commons.dbcp.BasicDataSource\"/></beans>";
		RootSpringXmlParser parser =  new RootSpringXmlParser(new ByteArrayInputStream(xml.getBytes()));
		try {
			parser.getDbUsername();
			fail();
		} catch (SqlConfigIncompleteException e) {
			//expected.
		}
		xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"org.apache.commons.dbcp.BasicDataSource\" p:username=\"test\"/></beans>";
		parser.appendSpringXmlConfig(new ByteArrayInputStream(xml.getBytes()));
		assertTrue("test".equals(parser.getDbUsername()));
	}

}
