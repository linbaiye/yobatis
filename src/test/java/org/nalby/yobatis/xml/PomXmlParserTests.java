package org.nalby.yobatis.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.nalby.yobatis.exception.UnsupportedProjectException;

public class PomXmlParserTests {
	
	@Test
	public void testNoVersionConfigured() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "</dependency>"
				+ "</dependencies></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue(null == tmp);
		
		xml = "<project><artifactId>test</artifactId><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>${mysql.version}</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue(null == tmp);
	}
	
	@Test
	public void testDirectConfiguredVersion() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>5.4.9</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue("mysql/mysql-connector-java/5.4.9/mysql-connector-java-5.4.9.jar".equals(tmp));
	}

	@Test
	public void testVersionVariable() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><properties>"
				+ "<mysql.version>5.4.1</mysql.version></properties>"
				+ "<dependencies><dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>${mysql.version}</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue("mysql/mysql-connector-java/5.4.1/mysql-connector-java-5.4.1.jar".equals(tmp));
	}
	
	@Test
	public void testNoProfileProperty() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><profiles><profile><id>develop</id><activation><activeByDefault>true</activeByDefault>"
				+ "</activation><properties><uplending.jdbc.datasource.type></uplending.jdbc.datasource.type>" + 
		    "</properties></profile></profiles></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(null == parser.getProfileProperty("uplending.jdbc.datasource.type"));
	}
	
	@Test
	public void testProfileProperty() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><profiles><profile><id>develop</id><activation><activeByDefault>true</activeByDefault>"
				+ "</activation><properties><uplending.jdbc.datasource.type>test</uplending.jdbc.datasource.type>"
				+ "<type>test</type>" + 
		    "</properties></profile></profiles></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue("test".equals(parser.getProfileProperty("uplending.jdbc.datasource.type")));
		assertTrue("test".equals(parser.getProfileProperty("type")));
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void testNoArtifactId() throws DocumentException, IOException {
		String xml = "<project><profiles><profile><id>develop</id><activation><activeByDefault>true</activeByDefault>"
				+ "</activation><properties><uplending.jdbc.datasource.type>test</uplending.jdbc.datasource.type>"
				+ "<type>test</type>" + 
		    "</properties></profile></profiles></project>";
			new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void testEmptyArtifactId() throws DocumentException, IOException {
		String xml = "<project><artifactId></artifactId><profiles><profile><id>develop</id><activation><activeByDefault>true</activeByDefault>"
				+ "</activation><properties><uplending.jdbc.datasource.type>test</uplending.jdbc.datasource.type>"
				+ "<type>test</type>" + 
		    "</properties></profile></profiles></project>";
			new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
	}
	
	@Test
	public void testArtifactId() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><profiles><profile><id>develop</id><activation><activeByDefault>true</activeByDefault>"
				+ "</activation><properties><uplending.jdbc.datasource.type>test</uplending.jdbc.datasource.type>"
				+ "<type>test</type>" + 
		    "</properties></profile></profiles></project>";
			PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
			assertTrue(parser.artifactIdEquals("test"));
	}
}
