package org.nalby.yobatis.structure.mybatis;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.mybatis.MybatisGeneratorAnalyzer;
import org.nalby.yobatis.mybatis.MybatisGeneratorContext;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.TestUtil;

public class MybatisGeneratorContextTests {
	
	private DatabaseMetadataProvider mockedSql;
	
	private MybatisGeneratorContext context;
	
	private Folder dao;
	
	private Folder model;
	
	private Folder resource;
	
	private static class Attribute {
		public String name;
		public String value;
		public Attribute(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	
	@Before
	public void setup() {
		mockedSql = mock(DatabaseMetadataProvider.class);
		when(mockedSql.getPassword()).thenReturn("password");
		when(mockedSql.getUsername()).thenReturn("username");
		when(mockedSql.getSchema()).thenReturn("schema");
		when(mockedSql.getDriverClassName()).thenReturn("driverClassName");
		when(mockedSql.getUrl()).thenReturn("url");
		when(mockedSql.getConnectorJarPath()).thenReturn("mysql.jar");
		context = new MybatisGeneratorContext("id", mockedSql);
		dao = TestUtil.mockFolder("/src/main/java/yobatis/dao");
		model = TestUtil.mockFolder("/src/main/java/yobatis/model");
		resource = TestUtil.mockFolder("/src/main/resource");
	}
	
	
	private void assertElement(Element element, String tag, Attribute ... attributes) {
		assertTrue(element.getName().equals(tag));
		for (Attribute attribute : attributes) {
			assertTrue(element.attributeValue(attribute.name).equals(attribute.value));
		}
	}

	
	@Test
	public void hasPredefinedElements() {
		Element element = context.getContext();
		assertTrue(element.elements("plugin").size() == 2);
		assertTrue(element.element("javaTypeResolver") != null);
		assertTrue(element.element("jdbcConnection") != null);
		context = new MybatisGeneratorContext(null, mockedSql);
		element = context.getContext();
		assertTrue("".equals(element.attributeValue("id")));
	}
	
	@Test
	public void appendNullFolders() {
		context.appendSqlMapGenerator(null);
		context.appendJavaModelGenerator(null);
		Element element = context.getContext();
		Element client = element.element(MybatisGeneratorAnalyzer.SQLMAP_GENERATOR_TAG);
		assertTrue(client.attributeValue("targetProject").equals(""));
		assertTrue(client.attributeValue("targetPackage").equals("mybatis-mappers"));

		Element model = element.element(MybatisGeneratorAnalyzer.MODEL_GENERATOR_TAG);
		assertTrue(model.attributeValue("targetProject").equals(""));
		assertTrue(model.attributeValue("targetPackage").equals(""));
	}
	
	@Test
	public void appendFolders() {
		context.appendJavaClientGenerator(dao);
		context.appendJavaModelGenerator(model);
		context.appendSqlMapGenerator(resource);
		Element element = context.getContext();

		Element client = element.element(MybatisGeneratorAnalyzer.CLIENT_GENERATOR_TAG);
		assertTrue(client.attributeValue("targetProject").equals("/src/main/java"));
		assertTrue(client.attributeValue("targetPackage").equals("yobatis.dao"));

		Element model = element.element(MybatisGeneratorAnalyzer.MODEL_GENERATOR_TAG);
		assertTrue(model.attributeValue("targetProject").equals("/src/main/java"));
		assertTrue(model.attributeValue("targetPackage").equals("yobatis.model"));

		Element resource = element.element(MybatisGeneratorAnalyzer.SQLMAP_GENERATOR_TAG);
		assertTrue(resource.attributeValue("targetProject").equals("/src/main/resource"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void appendEmptyTables() {
		context.appendTables(Collections.EMPTY_LIST, null);
		assertTrue(context.getContext().elements("table").isEmpty());
	}
	
	@Test
	public void appendTables() {
		Table table1 = new Table("table1");
		table1.addAutoIncColumn("pk");
		table1.addPrimaryKey("pk");
		Table table2 = new Table("table2");

		context.appendTables(Arrays.asList(table1, table2), "schema");
		List<Element> elements = context.getContext().elements("table");
		assertTrue(elements.size() == 2);
		for (Element element : elements) {
			if (element.attributeValue("tableName").equals("table1")) {
				Element pk = element.elements().iterator().next();
				assertElement(pk, "generatedKey", new Attribute("column", "pk"), new Attribute("sqlStatement", "mysql"));
			}
		}
	}

}