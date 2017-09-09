package org.nalby.yobatis.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.junit.Test;

public class SpringSuperXmlParserTests {
	private final static String[] DATASOURCE_CLASSES = {"org.apache.commons.dbcp.BasicDataSource", "com.alibaba.druid.pool.DruidDataSource"};

	@Test(expected = DocumentException.class)
	public void testWithInvalidXml() throws DocumentException, IOException {
		new SpringSuperXmlParser(new ByteArrayInputStream("<bean></bean>".getBytes()));
	}
	
	@Test
	public void testNoDatasouce() throws DocumentException, IOException {
		SpringSuperXmlParser parser =  new SpringSuperXmlParser(new ByteArrayInputStream("<beans><bean class=\"org.test.Clazz\" /></beans>".getBytes()));
		assertTrue(parser.getDbUsername() == null);
		parser =  new SpringSuperXmlParser(new ByteArrayInputStream("<beans:beans xmlns:beans=\"http://test.com/beans\"></beans:beans>".getBytes()));
		assertTrue(parser.getDbUsername() == null);
	}
	
	@Test
	public void testNoUsername() throws DocumentException, IOException {
		String xml = "<beans><bean class=\"org.apache.commons.dbcp.BasicDataSource\" /></beans>";
		SpringSuperXmlParser parser =  new SpringSuperXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getDbUsername() == null);
	}
	
	@Test
	public void testPNamespaceUsername() throws DocumentException, IOException {
		for (String clazz: DATASOURCE_CLASSES) {
			String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"" +
					clazz + "\" p:username=\"test\"/></beans>";
			SpringSuperXmlParser parser =  new SpringSuperXmlParser(new ByteArrayInputStream(xml.getBytes()));
			assertTrue("test".equals(parser.getDbUsername()));
		}
	}
	
	@Test
	public void testProperyUsername() throws DocumentException, IOException {
		for (String clazz: DATASOURCE_CLASSES) {
			String xml = "<beans xmlns:p=\"http://www.springframework.org/schema/p\"><bean class=\"" +
			clazz + "\"><property name=\"username\" value=\"test\"/></bean></beans>";
			SpringSuperXmlParser parser =  new SpringSuperXmlParser(new ByteArrayInputStream(xml.getBytes()));
			assertTrue("test".equals(parser.getDbUsername()));
		}
	}

}
