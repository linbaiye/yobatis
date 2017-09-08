package org.nalby.yobatis.xml;


import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import org.dom4j.DocumentException;
import org.junit.Test;

public class WebXmlParserTests {

	@Test
	public void testWithInvaidXml() throws IOException, DocumentException {
		try {
			new WebXmlParser(new ByteArrayInputStream("xml".getBytes()));
			fail();
		} catch (DocumentException e) {
			//Expected.
		}
		try {
			new WebXmlParser(new ByteArrayInputStream("<xml></xml>".getBytes()));
			fail();
		} catch (DocumentException e) {
			//Expected.
		}
	}
	
	@Test
	public void testInvalidContextParam() throws IOException, DocumentException  {
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream("<web-app></web-app>".getBytes()));
		assertTrue(parser.getAppConfigLocation() == null);
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "</web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		try {
			parser.getAppConfigLocation();
			fail();
		} catch (DocumentException exception) {
			assertTrue(exception.getMessage().indexOf("Multiple contextConfigLocation") != -1);
		}
	}
	
	@Test
	public void testExpectedContextParam() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "</web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue("test".equals(parser.getAppConfigLocation()));
		
		//Empty value is ok.
		xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value></param-value></context-param>"
				+ "</web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue("".equals(parser.getAppConfigLocation()));
		
		//It's ok not to configure contextConfigLocation.
		xml = "<web-app><context-param></context-param></web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getAppConfigLocation() == null);
	}
	
	@Test
	public void testNoServlets() throws DocumentException, IOException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getServletConfigLocation();
		assertTrue(locations.isEmpty());
	}

	@Test
	public void testDuplicatedLocations() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value></param-value></init-param>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value></param-value></init-param>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		try {
			parser.getServletConfigLocation();
			fail();
		} catch (DocumentException e) {
			//Expected.
		}
		xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>path</param-value></init-param>"
				+ "</servlet>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>path</param-value></init-param>"
				+ "</servlet>"
				+ "</web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		try {
			parser.getServletConfigLocation();
			fail();
		} catch (DocumentException e) {
			//Expected.
		}
	}

	@Test
	public void testMultiLocations() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>loc1</param-value></init-param>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>loc2</param-value></init-param>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		try {
			parser.getServletConfigLocation();
			fail();
		} catch (DocumentException e) {
			//Expected.
		}
		xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>loc1</param-value></init-param>"
				+ "</servlet>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>loc2</param-value></init-param>"
				+ "</servlet>"
				+ "</web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> locations = parser.getServletConfigLocation();
		assertTrue(locations.size() == 2 && locations.contains("loc1") && locations.contains("loc2"));
	}
	
	
}
