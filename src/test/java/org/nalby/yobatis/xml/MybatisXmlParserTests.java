package org.nalby.yobatis.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Comment;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MybatisXmlParserTests {
	
	private MybatisConfigFileGenerator mockedGenerator;

	private DocumentFactory documentFactory = DocumentFactory.getInstance();
	
	private Element classpath;
	
	private Set<Element> javaModelGenerators;

	private Set<Element> sqlMapGenerators;

	private Set<Element> javaClientGenerators;

	private Set<Element> tables;
	
	private Element context;
	
	private Element javaTypeResolver;

	private Element pluginElement;
	
	private Element criteriaPluginElement;
	
	private void initClasspath(String location) {
		classpath = documentFactory.createElement("classPathEntry");
		classpath.addAttribute("location", location);
	}

	public void initContext() {
		context = documentFactory.createElement("context");
	}
	
	public void initJavaTypeResolver() {
		javaTypeResolver = documentFactory.createElement("javaTypeResolver");
		Element property = javaTypeResolver.addElement("property");
		property.addAttribute("name", "name");
		property.addAttribute("value", "value");
	}

	private void initJavaModelGenerators() {
		javaModelGenerators = new HashSet<Element>();
		Element element = documentFactory.createElement("javaModelGenerator");
		element.addAttribute("targetPackage", "targetPackage1");
		element.addAttribute("targetProject", "targetProject1");
		javaModelGenerators.add(element);
		element = documentFactory.createElement("javaModelGenerator");
		element.addAttribute("targetPackage", "targetPackage2");
		element.addAttribute("targetProject", "targetProject2");
		javaModelGenerators.add(element);
	}
	
	private void initSqlMapGenerators() {
		sqlMapGenerators = new HashSet<Element>();
		Element element = documentFactory.createElement("sqlMapGenerator");
		element.addAttribute("targetPackage", "targetPackage1");
		element.addAttribute("targetProject", "targetProject1");
		sqlMapGenerators.add(element);
		element = documentFactory.createElement("sqlMapGenerator");
		element.addAttribute("targetPackage", "targetPackage2");
		element.addAttribute("targetProject", "targetProject2");
		sqlMapGenerators.add(element);
	}
	
	private void initJavaClientGenerators() {
		javaClientGenerators = new HashSet<Element>();
		Element element = documentFactory.createElement("javaClientGenerator");
		element.addAttribute("targetPackage", "targetPackage1");
		element.addAttribute("targetProject", "targetProject1");
		javaClientGenerators.add(element);
		element = documentFactory.createElement("javaClientGenerator");
		element.addAttribute("targetPackage", "targetPackage2");
		element.addAttribute("targetProject", "targetProject2");
		javaClientGenerators.add(element);
	}
	
	private void initTables() {
		tables = new HashSet<Element>();
		Element element = documentFactory.createElement("table");
		element.addAttribute("tableName", "table1");
		element.addAttribute("schema", "schema");
		tables.add(element);
		element = documentFactory.createElement("table");
		element.addAttribute("tableName", "table2");
		element.addAttribute("schema", "schema");
		tables.add(element);
	}
	
	private void initPluginElement() {
		pluginElement = documentFactory.createElement("plugin");
		pluginElement.addAttribute("type", "org.mybatis.generator.plugins.YobatisPlugin");
	}
	
	private void initCriteriaPluginElement() {
		criteriaPluginElement = documentFactory.createElement("plugin");
		criteriaPluginElement.addAttribute("type", "org.mybatis.generator.plugins.YobatisCriteriaPlugin");
	}
	
	public void resetGenerator() {
		mockedGenerator = mock(MybatisConfigFileGenerator.class);
		when(mockedGenerator.getClassPathEntryElement()).thenReturn(classpath);
		when(mockedGenerator.getContext()).thenReturn(context);
		when(mockedGenerator.getJavaTypeResolverElement()).thenReturn(javaTypeResolver);
		when(mockedGenerator.getJavaModelGeneratorElements()).thenReturn(javaModelGenerators);
		when(mockedGenerator.getSqlMapGeneratorElements()).thenReturn(sqlMapGenerators);
		when(mockedGenerator.getJavaClientGeneratorElements()).thenReturn(javaClientGenerators);
		when(mockedGenerator.getTableElements()).thenReturn(tables);
		when(mockedGenerator.getPluginElement()).thenReturn(pluginElement);
		when(mockedGenerator.getCriteriaPluginElement()).thenReturn(criteriaPluginElement);
	}

	@Before
	public void setup() {
		initClasspath("test");
		initContext();
		initJavaTypeResolver();
		initJavaModelGenerators();
		initSqlMapGenerators();
		initJavaClientGenerators();
		initTables();
		initPluginElement();
		initCriteriaPluginElement();
		resetGenerator();
	}

	private class DocXml extends AbstractXmlParser {
		private Element root;
		private Element classpathEntry;
		private List<Element> javaModelGenerators;
		private Element context;
		private List<Element> tables;
		private List<Element> plugins;
		public DocXml(InputStream inputStream)
				throws DocumentException, IOException {
			super(inputStream, "generatorConfiguration");
			root = document.getRootElement();
			classpathEntry = root.element("classPathEntry");
			context = root.element("context");
			javaModelGenerators = context.elements("javaModelGenerator");
			tables = context.elements("table");
			plugins = context.elements("plugin");
		}
		public void assertClasspathEntry(String location) {
			assertTrue(classpathEntry.attributeValue("location").equals(location));
		}

		public boolean hasJavaModelGenerator(String targetPackage, String targetProject) {
			for (Element javaModelGenerator: this.javaModelGenerators) {
				if (javaModelGenerator.attributeValue("targetPackage").equals(targetPackage) && 
						javaModelGenerator.attributeValue("targetProject").equals(targetProject)) {
					return true;
				}
			}
			return false;
		}
		
		
		public boolean hasTable(String tableName, String schema) {
			for (Element table: this.tables) {
				if (table.attributeValue("tableName").equals(tableName) &&
						table.attributeValue("schema").equals(schema)) {
					return true;
				}
			}
			return false;
		}
		
		
	
		public int countPlugin(String type) {
			int counter = 0;
			for (Element plugin: plugins) {
				if (type.equals(plugin.attributeValue("type"))) {
					++counter;
				}
			}
			return counter;
		}
		
		public boolean hasPlugin(String type) {
			return countPlugin(type) > 0;
		}
		
		
		public boolean hasSinglePlugin(String type) {
			if (countPlugin(type) != 1) {
				return false;
			}
			return true;
		}
		
		public boolean hasSinglePlugin(String type, String propName, String propValue) {
			if (countPlugin(type) != 1) {
				return false;
			}
			for (Element plugin: plugins) {
				if (type.equals(plugin.attributeValue("type"))) {
					for (Element property: plugin.elements("property")) {
						if (propName.equals(property.attributeValue("name")) &&
							propValue.equals(property.attributeValue("value"))) {
							return true;
						}
					}
				}
			}
			return false;
		}
		
		
		public boolean hasCommentedElement(String name) {
			for (Iterator<Node> iterator = context.nodeIterator(); iterator.hasNext(); ) {
				Node node = iterator.next();
				if (node.getNodeType() == Node.COMMENT_NODE) {
					Comment comment = (Comment)node;
					if (comment.getText().startsWith(name)) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	@Test
	public void testMergeClasspathWhenExisted() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <classPathEntry location=\"oldLocation\"/>\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <javaModelGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n" + 
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		newDoc.assertClasspathEntry("oldLocation");
	}
	
	@Test
	public void testMergeClasspathWhenNotExisted() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <!--classPathEntry location=\"oldLocation\"/-->\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <javaModelGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n" + 
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		newDoc.assertClasspathEntry("test");
	}
	
	@Test
	public void testMergeGeneratorWhenExisted() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <!--classPathEntry location=\"oldLocation\"/-->\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <javaModelGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n" + 
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(newDoc.hasJavaModelGenerator("targetPackage1", "targetProject1"));
		assertTrue(newDoc.hasCommentedElement("javaModelGenerator"));
	}
	
	@Test
	public void testMergeGeneratorWhenNotExisted() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <!--classPathEntry location=\"oldLocation\"/-->\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <!--javaModelGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/-->\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n" + 
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		resetGenerator();
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(newDoc.hasJavaModelGenerator("targetPackage1", "targetProject1"));
		assertTrue(newDoc.hasJavaModelGenerator("targetPackage2", "targetProject2"));
	}
	
	
	@Test
	public void testMergeTablesWhenNotExisted() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <!--classPathEntry location=\"oldLocation\"/-->\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <!--javaModelGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/-->\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n" + 
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(newDoc.hasTable("table1", "schema"));
		assertTrue(newDoc.hasTable("table2", "schema"));
	}
	
	@Test
	public void testSomeTablesCommented() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <classPathEntry location=\"oldLocation\"/>\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <table tableName=\"table1\" schema=\"schema\" />\n" +
				"    <!--table tableName=\"table2\" schema=\"schema\" /-->\n" +
				"  </context>\n" +
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(newDoc.hasTable("table1", "schema"));
		assertTrue(!newDoc.hasTable("table2", "schema"));
	}

	@Test
	public void testAllTablesCommented() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <classPathEntry location=\"oldLocation\"/>\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <!--table tableName=\"table1\" schema=\"schema\" />\n" +
				"    <table tableName=\"table2\" schema=\"schema\" /-->\n" +
				"  </context>\n" +
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(!newDoc.hasTable("table1", "schema"));
		assertTrue(!newDoc.hasTable("table2", "schema"));
	}
	
	@Test
	public void existentConfigHasNoPlugins() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <classPathEntry location=\"oldLocation\"/>\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <!--table tableName=\"table1\" schema=\"schema\" />\n" +
				"    <table tableName=\"table2\" schema=\"schema\" /-->\n" +
				"  </context>\n" +
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(newDoc.hasSinglePlugin("org.mybatis.generator.plugins.YobatisPlugin"));
		assertTrue(newDoc.hasSinglePlugin("org.mybatis.generator.plugins.YobatisCriteriaPlugin"));
	}
	
	@Test
	public void existentConfigCommentsYobatisPlugin() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <classPathEntry location=\"oldLocation\"/>\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"  <!--plugin type=\"org.mybatis.generator.plugins.YobatisPlugin\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"true\"/>\n" + 
				"    </plugin-->\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n" +
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(newDoc.hasSinglePlugin("org.mybatis.generator.plugins.YobatisPlugin"));
	}
	
	@Test
	public void existentConfigHasPlugins() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <classPathEntry location=\"oldLocation\"/>\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"  	 <plugin type=\"org.mybatis.generator.plugins.YobatisPlugin\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"false\"/>\n" + 
				"  	 </plugin>" + 
				"  	 <plugin type=\"org.mybatis.generator.plugins.YobatisCriteriaPlugin\" />\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n" +
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(newDoc.hasSinglePlugin("org.mybatis.generator.plugins.YobatisPlugin", "enableBaseClass", "false"));
		assertTrue(newDoc.hasSinglePlugin("org.mybatis.generator.plugins.YobatisCriteriaPlugin"));
	}
	

	@Test
	public void existentConfigHasCommentedCriteiraPlugin() throws DocumentException, IOException {
		String xmldoc = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"\n" + 
				"<generatorConfiguration>\n" + 
				"  <classPathEntry location=\"oldLocation\"/>\n" + 
				"  <context id=\"mysqlTables\" targetRuntime=\"MyBatis3\">\n" + 
				"  	 <plugin type=\"org.mybatis.generator.plugins.YobatisPlugin\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"false\"/>\n" + 
				"  	 </plugin>" + 
				"  	 <!--plugin type=\"org.mybatis.generator.plugins.YobatisCriteriaPlugin\" /-->\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n" +
				"</generatorConfiguration>\n";
		MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(new ByteArrayInputStream(xmldoc.getBytes()));
		mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(mockedGenerator);
		String tmp = mybatisXmlParser.asXmlText();
		DocXml newDoc = new DocXml(new ByteArrayInputStream(tmp.getBytes()));
		assertTrue(newDoc.hasSinglePlugin("org.mybatis.generator.plugins.YobatisPlugin", "enableBaseClass", "false"));
		assertTrue(!newDoc.hasPlugin("org.mybatis.generator.plugins.YobatisCriteriaPlugin"));
		assertTrue(newDoc.hasCommentedElement("plugin"));
	}
}
