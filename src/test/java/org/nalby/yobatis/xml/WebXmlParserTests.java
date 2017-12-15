package org.nalby.yobatis.xml;


import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.TestUtil;

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
		assertTrue(parser.getSpringInitParamValues().isEmpty());
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "</web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		parser.getSpringInitParamValues();
	}
	
	@Test
	public void validContextParam() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "</web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> result = parser.getSpringInitParamValues();
		TestUtil.assertCollectionSizeAndStringsIn(result, 1, "test");
		

	}

	@Test
	public void emptyServletInitParam() throws DocumentException, IOException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getSpringInitParamValues().isEmpty());

		xml = "<web-app><context-param></context-param></web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getSpringInitParamValues().isEmpty());
	}

	@Test(expected = UnsupportedProjectException.class)
	public void testMultilpleServletLocations() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value></param-value></init-param>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value></param-value></init-param>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getSpringInitParamValues().isEmpty());
		xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>path</param-value></init-param>"
				+ "</servlet>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>path</param-value></init-param>"
				+ "</servlet>"
				+ "</web-app>";
		parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		parser.getSpringInitParamValues();
	}

	@Test
	public void testCorrectServletLocation() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>loc1</param-value></init-param>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> result = parser.getSpringInitParamValues();
		TestUtil.assertCollectionSizeAndStringsIn(result, 1, "loc1");
	}
	
	@Test
	public void testMixedConfigLocations() throws IOException, DocumentException {
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "<servlet><servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>"
				+ "<init-param><param-name>contextConfigLocation</param-name><param-value>loc1</param-value></init-param>"
				+ "</servlet></web-app>";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> result = parser.getSpringInitParamValues();
		TestUtil.assertCollectionSizeAndStringsIn(result, 2, "test", "loc1");
	}
	
	@Test
	public void locationsWithSeperator () throws IOException, DocumentException {
		String xml = "<web-app>\n" + 
				"  <context-param>\n" + 
				"    <param-name>contextConfigLocation</param-name>\n" + 
				"    <param-value>\n" + 
				"      test\n" + 
				"      path\n" + 
				"      test1\n" + 
				"    </param-value>\n" + 
				"    </context-param>\n" + 
				"</web-app>\n" + 
				"";
		WebXmlParser parser = new WebXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> result = parser.getSpringInitParamValues();
		assertTrue(result.size() == 1);
		for (String tmp : result) {
			assertTrue(tmp.contains("test"));
			assertTrue(tmp.contains("test1"));
			assertTrue(tmp.contains("path"));
		}
	}

}
