package org.nalby.yobatis.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.junit.Test;

public class RootPomXmlParserTests {
	
	@Test
	public void testNoVersionConfigured() throws DocumentException, IOException {
		String xml = "<project><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "</dependency>"
				+ "</dependencies></project>";
		RootPomXmlParser parser = new RootPomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue(null == tmp);
		
		xml = "<project><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>${mysql.version}</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		parser = new RootPomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue(null == tmp);
	}
	
	@Test
	public void testDirectConfiguredVersion() throws DocumentException, IOException {
		String xml = "<project><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>5.4.9</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		RootPomXmlParser parser = new RootPomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue("mysql/mysql-connector-java/5.4.9/mysql-connector-java-5.4.9.jar".equals(tmp));
	}

	@Test
	public void testVersionVariable() throws DocumentException, IOException {
		String xml = "<project><properties>"
				+ "<mysql.version>5.4.1</mysql.version></properties>"
				+ "<dependencies><dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>${mysql.version}</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		RootPomXmlParser parser = new RootPomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue("mysql/mysql-connector-java/5.4.1/mysql-connector-java-5.4.1.jar".equals(tmp));
	}
}
