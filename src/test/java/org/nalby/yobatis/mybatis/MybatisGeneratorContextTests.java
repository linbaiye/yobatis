package org.nalby.yobatis.mybatis;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.mybatis.MybatisGeneratorContext;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.TestUtil;
import org.nalby.yobatis.xml.AbstractXmlParser;

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
	
	private class XmlHelper extends AbstractXmlParser  {
		public XmlHelper(InputStream inputStream) throws DocumentException, IOException {
			super(inputStream, "context");
		}
		public Element getRoot() {
			return document.getRootElement();
		}
	}
	
	private MybatisGeneratorContext build(String xml) {
		try {
			Element tmp = new XmlHelper(new ByteArrayInputStream(xml.getBytes())).getRoot();
			assertTrue(tmp != null);
			return new MybatisGeneratorContext(tmp);
		} catch (DocumentException | IOException e) {
			throw new IllegalAccessError("bad xml.");
		}
	}
	
	
	/**
	 * Assert that the element is of named tag and has the attributes.
	 * @param element
	 * @param tag
	 * @param attributes
	 */
	private void assertElement(Element element, String tag, Attribute ... attributes) {
		assertTrue(element.getName().equals(tag));
		for (Attribute attribute : attributes) {
			assertTrue(element.attributeValue(attribute.name).equals(attribute.value));
		}
	}
	
	private void assertHasAttributes(Element element, Attribute ... attributes) {
		for (Attribute attribute : attributes) {
			assertTrue(element.attributeValue(attribute.name).equals(attribute.value));
		}
	}
	
	private void assertTables(List<Element> tables, String ... names) {
		assertTrue(tables.size() == names.length);
		for (String name : names) {
			boolean found = false;
			for (Element element : tables) {
				if (!found) {
					found = element.attributeValue("tableName").equals(name);
				}
			}
			assertTrue(found);
		}
	}
	
	
	private boolean hasPlugin(List<Element> elements, String type) {
		for (Element element : elements) {
			if (type.equals(element.attributeValue("type"))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasTable(List<Element> tableElements, String name) {
		for (Element thisTable : tableElements) {
			if (name != null && name.equals(thisTable.attributeValue("tableName"))) {
				return true;
			}
		}
		return false;
	}
	
	private void assertPluginElement(Element element) {
		assertTrue(element.elements("plugin").size() != 0);
		assertTrue(element.element("javaTypeResolver") != null);
		assertTrue(element.element("jdbcConnection") != null);
	}

	
	@Test
	public void hasPredefinedElements() {
		Element element = context.getContext();
		assertTrue(element.elements("plugin").size() == 2);
		assertPluginElement(element);
	}

	
	@Test
	public void appendNullFolders() {
		context.createSqlMapGenerator(null);
		context.createJavaModelGenerator(null);
		Element element = context.getContext();
		Element client = element.element(MybatisGeneratorContext.SQLMAP_GENERATOR_TAG);
		assertTrue(client.attributeValue("targetProject").equals(""));
		assertTrue(client.attributeValue("targetPackage").equals("mybatis-mappers"));

		Element model = element.element(MybatisGeneratorContext.MODEL_GENERATOR_TAG);
		assertTrue(model.attributeValue("targetProject").equals(""));
		assertTrue(model.attributeValue("targetPackage").equals(""));
	}
	
	@Test
	public void appendFolders() {
		context.createJavaClientGenerator(dao);
		context.createJavaModelGenerator(model);
		context.createSqlMapGenerator(resource);
		Element element = context.getContext();

		Element client = element.element(MybatisGeneratorContext.CLIENT_GENERATOR_TAG);
		assertTrue(client.attributeValue("targetProject").equals("/src/main/java"));
		assertTrue(client.attributeValue("targetPackage").equals("yobatis.dao"));

		Element model = element.element(MybatisGeneratorContext.MODEL_GENERATOR_TAG);
		assertTrue(model.attributeValue("targetProject").equals("/src/main/java"));
		assertTrue(model.attributeValue("targetPackage").equals("yobatis.model"));

		Element resource = element.element(MybatisGeneratorContext.SQLMAP_GENERATOR_TAG);
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
	
	
	/*
	 * Below are tests linked to creating from existent context.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void emptyId() {
		String xml = 
				"  <context id=\"\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <javaModelGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <sqlMapGenerator targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"    <javaClientGenerator type=\"XMLMAPPER\" targetPackage=\"targetPackage1\" targetProject=\"targetProject1\"/>\n" + 
				"  </context>\n";
		build(xml);
	}


	
	@Test
	public void hasElements() {
		String xml = 
				"  <context id=\"id\" targetRuntime=\"MyBatis3\">\n" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"  </context>\n";
		MybatisGeneratorContext context = build(xml);
		assertPluginElement(context.getContext());
		xml = 
				"  <context id=\"id\" targetRuntime=\"MyBatis3\">\n" + 
				"  	 <plugin type=\"org.mybatis.generator.plugins.YobatisPlugin\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"true\"/>\n" + 
				"    </plugin>" + 
				"  	 <plugin type=\"test\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"true\"/>\n" + 
				"    </plugin>" + 
				"    <jdbcConnection driverClass=\"com.mysql.jdbc.Driver\" connectionURL=\"jdbc:mysql://127.0.0.1:3306/uplending?characterEncoding=utf-8\" userId=\"uplending\" password=\"uplendingxwg370\"/>\n" + 
				"    <javaTypeResolver>\n" + 
				"      <property name=\"forceBigDecimals\" value=\"false\"/>\n" + 
				"    </javaTypeResolver>\n" + 
				"    <table tableName=\"table1\" schema=\"schema\" />\n" +
				"    <table tableName=\"table2\" schema=\"schema\" />n" +
				"  </context>\n";
		context = build(xml);
		assertPluginElement(context.getContext());
		assertTrue(hasPlugin(context.getContext().elements("plugin"), MybatisGeneratorContext.YOBATIS_PLUGIN));
		assertTrue(hasPlugin(context.getContext().elements("plugin"), "test"));
		assertTrue(hasTable(context.getContext().elements("table"), "table1"));
		assertTrue(hasTable(context.getContext().elements("table"), "table2"));
		assertTrue(context.hasTable());
	}
	
	
	@Test
	public void mergeTables() {
		String xml = 
				"  <context id=\"id\" targetRuntime=\"MyBatis3\">\n" + 
				"  	 <plugin type=\"org.mybatis.generator.plugins.YobatisPlugin\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"true\"/>\n" + 
				"    </plugin>" + 
				"  	 <plugin type=\"test\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"true\"/>\n" + 
				"    </plugin>" + 
				"    <table tableName=\"table1\" schema=\"schema\" />\n" +
				"    <table tableName=\"table3\" schema=\"schema\" />\n" +
				"    <table tableName=\"table4\" schema=\"schema\" />n" +
				"  </context>\n";
		appendTables();
		MybatisGeneratorContext thisContext = build(xml);
		thisContext.idEqualsTo(context);
		thisContext.merge(context);
		Element element = thisContext.getContext();
		assertTrue(element.elements("table").size() == 4);
		assertTrue(hasTable(element.elements("table"), "table1"));
		assertTrue(hasTable(element.elements("table"), "table2"));
		assertTrue(hasTable(element.elements("table"), "table3"));
		assertTrue(hasTable(element.elements("table"), "table4"));
		assertHasAttributes(element.element("jdbcConnection"), new Attribute("driverClass", "driverClassName"));
	}
	
	
	@Test
	public void preserveCommentedTables() {
		String xml = 
				"  <context id=\"id\" targetRuntime=\"MyBatis3\">\n" + 
				"    <!--table tableName=\"table3\" schema=\"schema\" />\n" +
				"    <table tableName=\"table4\" schema=\"schema\" /-->\n" +
				"    <!--table tableName=\"table2\" schema=\"schema\" /-->\n" +
				"    <!--table 		tableName=\"table5\" schema=\"schema\" /-->" +
				"    <!--able 		tableName=\"table5\" schema=\"schema\" /-->" +
				"  </context>\n";
		MybatisGeneratorContext thatContext = build(xml);
		List<Element> commentd = AbstractXmlParser.loadCommentedElements(thatContext.getContext());
		assertTables(commentd, "table3", "table4", "table5", "table2");
	}
	
	@Test
	public void removeExistentTables() {
		String xml = 
				"  <context id=\"id\" targetRuntime=\"MyBatis3\">\n" + 
				"  	 <plugin type=\"org.mybatis.generator.plugins.YobatisPlugin\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"true\"/>\n" + 
				"    </plugin>" + 
				"  	 <plugin type=\"test\">\n" + 
				"        <property name=\"enableBaseClass\" value=\"true\"/>\n" + 
				"    </plugin>" + 
				"    <table tableName=\"table1\" schema=\"schema\" />\n" +
				"    <!--table tableName=\"table3\" schema=\"schema\" />\n" +
				"    <table tableName=\"table4\" schema=\"schema\" /-->\n" +
				"    <!--table tableName=\"table2\" schema=\"schema\" /-->\n" +
				"    <!--table 		tableName=\"table5\" schema=\"schema\" /-->" +
				"    <!--able 		tableName=\"table5\" schema=\"schema\" /-->" +
				"  </context>\n";
		Table table1 = new Table("table1");
		Table table2 = new Table("table2");
		context.appendTables(Arrays.asList(table1, table2), "schema");
		MybatisGeneratorContext thatContext = build(xml);
		context.removeExistentTables(thatContext);
		assertTrue(context.getContext().elements("table").isEmpty());
	}
}