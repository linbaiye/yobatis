package org.nalby.yobatis.xml;


import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.nalby.yobatis.exception.UnsupportedProjectException;

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
	
	@Test(expected = UnsupportedProjectException.class)
	public void testInvalidContextParam() throws IOException, DocumentException  {
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream("<web-app></web-app>".getBytes()));
		try {
			parser.getSpringConfigLocations();
			fail();
		}  catch (UnsupportedProjectException e) {
			//expected.
		}
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "</web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		parser.getSpringConfigLocations();
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void testExpectedContextParam() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "</web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		List<String> result = parser.getSpringConfigLocations();
		assertTrue(result.size() == 1 && result.get(0).equals("test"));
		
		//It's ok not to configure contextConfigLocation.
		xml = "<web-app><context-param></context-param></web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		//Let it throw.
		parser.getSpringConfigLocations();
	}
	
	@Test(expected = UnsupportedProjectException.class)
	public void testEmptyValueInServletConfig() throws DocumentException, IOException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		parser.getSpringConfigLocations();
	}

	@Test(expected = UnsupportedProjectException.class)
	public void testMultilpleServletLocations() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value></param-value></init-param>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value></param-value></init-param>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		try {
			parser.getSpringConfigLocations();
			fail();
		} catch (UnsupportedProjectException e) {
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
		parser.getSpringConfigLocations();
	}

	@Test
	public void testCorrectServletLocation() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>loc1</param-value></init-param>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		List<String> result = parser.getSpringConfigLocations();
		assertTrue(result.size() == 1 && "loc1".equals(result.get(0)));
	}
	
	@Test
	public void testMixedConfigLocations() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>loc1</param-value></init-param>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		List<String> result = parser.getSpringConfigLocations();
		assertTrue(result.size() == 2 && "test".equals(result.get(0)) && "loc1".equals(result.get(1)));
	}

}
