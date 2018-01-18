package org.nalby.yobatis.structure.mybatis;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.mybatis.MybatisGeneratorContext;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlCreator;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlReader;

public class MybatisGeneratorXmlReaderTests {
	
	private MybatisGeneratorXmlReader reader;
	
	private MybatisGeneratorXmlCreator creator;
	
	private DocumentFactory factory = DocumentFactory.getInstance();
	
	private Element classpath;
	
	private List<MybatisGeneratorContext> contexts;

	@Before
	public void setup() {
		creator = mock(MybatisGeneratorXmlCreator.class);
		classpath = factory.createElement("classPathEntry");
		classpath.addAttribute("location", "new_location");
		when(creator.getClassPathEntryElement()).thenReturn(classpath);
		contexts = new LinkedList<>();
		when(creator.getContexts()).thenReturn(contexts);
	}
	
	/**
	 * Mock context and add it to contexts.
	 * @param id the context id.
	 */
	private void mockContext(String id, String ... tableNames) {
		Element element = factory.createElement("context");
		element.addAttribute("id", id);
		element.addAttribute("targetRuntime", "Mybatis3");
		if (tableNames != null) {
			for (String name : tableNames) {
				Element table = element.addElement("table");
				table.addAttribute("tableName", name);
			}
		}
		contexts.add(new MybatisGeneratorContext(element));
	}
	
	@Test(expected = InvalidMybatisGeneratorConfigException.class)
	public void badXml() {
		String xml = "<hello</helloe>";
		reader = MybatisGeneratorXmlReader.build(new ByteArrayInputStream(xml.getBytes()));
	}
	
	@Test
	public void validXml() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<!DOCTYPE generatorConfiguration PUBLIC \"-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN\" \"http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd\">\n" + 
				"<generatorConfiguration>\n" + 
				"  <classPathEntry location=\"/user/.m2/repository/mysql/mysql-connector-java/5.1.25/mysql-connector-java-5.1.25.jar\" />\n" + 
				"  <context id=\"hello\" targetRuntime=\"MyBatis3\">\n" + 
				"    <plugin type=\"org.mybatis.generator.plugins.YobatisPlugin\">\n" + 
				"	<property name=\"enableBaseClass\" value=\"true\"/>\n" + 
				"    </plugin>\n" + 
				"    <plugin type=\"org.mybatis.generator.plugins.YobatisCriteriaPlugin\" />\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\"\n" + 
				"        connectionURL=\"jdbc:mysql://127.0.0.1:3306/api?characterEncoding=utf-8\"\n" + 
				"        userId=\"root\"\n" + 
				"        password=\"root\">\n" + 
				"    </jdbcConnection>\n" + 
				"    <javaTypeResolver >\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\" />\n" + 
				"    </javaTypeResolver>\n" + 
				"    <javaModelGenerator targetPackage=\"hello.model\" targetProject=\"src/main/java\">\n" + 
				"      <property name=\"enableSubPackages\" value=\"true\" />\n" + 
				"      <property name=\"trimStrings\" value=\"true\" />\n" + 
				"    </javaModelGenerator>\n" + 
				"    <sqlMapGenerator targetPackage=\"mybatis\"  targetProject=\"src/main/resources\">\n" + 
				"      <property name=\"enableSubPackages\" value=\"true\" />\n" + 
				"    </sqlMapGenerator>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"hello.dao\" targetProject=\"src/main/java\">\n" + 
				"      <property name=\"enableSubPackages\" value=\"true\" />\n" + 
				"    </javaClientGenerator>\n" + 
				"    <table tableName=\"user\" modelType=\"flat\">\n" + 
				"    </table>\n" + 
				"  </context>\n" + 
				"</generatorConfiguration>";
		reader = MybatisGeneratorXmlReader.build(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(reader.asXmlText().contains("classPathEntry location"));
		assertTrue(reader.asXmlText().contains("context id="));
	}

	
	@Test
	public void merge() {
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<!DOCTYPE generatorConfiguration PUBLIC \"-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN\" \"http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd\">\n" + 
				"<generatorConfiguration>\n" + 
				"  <context id=\"hello\" targetRuntime=\"MyBatis3\"/>" + 
				"</generatorConfiguration>";
		reader = MybatisGeneratorXmlReader.build(new ByteArrayInputStream(xml.getBytes()));
		mockContext("hello", "table1");
		mockContext("contextid2");
		reader.mergeGeneratedConfig(creator);
		assertTrue(reader.asXmlText().contains("classPathEntry location"));
		assertTrue(reader.asXmlText().contains("table tableName"));
		assertTrue(reader.asXmlText().contains("contextid2"));
	}
	

}
