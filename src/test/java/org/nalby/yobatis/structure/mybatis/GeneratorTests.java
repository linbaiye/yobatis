package org.nalby.yobatis.structure.mybatis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.PomTree;
import org.nalby.yobatis.util.TestUtil;
import org.nalby.yobatis.xml.MybatisXmlParser;

public class GeneratorTests {
	
	private PomTree mockedPomTree;
	
	private Sql mockedSql;
	
	private List<Table> tables;
	
	private List<Folder> resouceFolders;

	private List<Folder> daoFolders;

	private List<Folder> modelFolders;
	
	private MybatisConfigFileGenerator generator;

	private static class Attribute {
		public String name;
		public String value;
		public Attribute(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	
	private Table mockTable(String name, String pk) {
		Table table = mock(Table.class);
		when(table.getName()).thenReturn(name);
		when(table.getAutoIncPK()).thenReturn(pk);
		return table;
	}

	
	@Before
	public void setup() {
		mockedPomTree = mock(PomTree.class);
		mockedSql = mock(Sql.class);
		when(mockedSql.getPassword()).thenReturn("password");
		when(mockedSql.getUsername()).thenReturn("username");
		when(mockedSql.getSchema()).thenReturn("schema");
		when(mockedSql.getDriverClassName()).thenReturn("driverClassName");
		when(mockedSql.getUrl()).thenReturn("url");
		when(mockedSql.getConnectorJarPath()).thenReturn("mysql.jar");
		tables = new LinkedList<>();
		when(mockedSql.getTables()).thenReturn(tables);
		
		resouceFolders = new LinkedList<>();
		when(mockedPomTree.lookupResourceFolders()).thenReturn(resouceFolders);
		
		daoFolders = new LinkedList<>();
		when(mockedPomTree.lookupDaoFolders()).thenReturn(daoFolders);
		
		modelFolders = new LinkedList<>();
		when(mockedPomTree.lookupModelFolders()).thenReturn(modelFolders);
	}
	
	
	private void build() {
		generator = new MybatisConfigFileGenerator(mockedPomTree, mockedSql);
	}
	
	
	private void assertElement(Element element, String tag, Attribute ... attributes) {
		assertTrue(element.getName().equals(tag));
		for (Attribute attribute : attributes) {
			assertTrue(element.attributeValue(attribute.name).equals(attribute.value));
		}
	}
	
	@Test
	public void classpath() {
		build();
		Element element = generator.getClassPathEntryElement();
		assertElement(element, MybatisXmlParser.CLASS_PATH_ENTRY_TAG, new Attribute("location", "mysql.jar"));
	}
	
	@Test
	public void javaClientGenerators() {
		Folder folder = TestUtil.mockFolder("/yobatis/src/main/java/dao");
		daoFolders.add(folder);
		build();
		Set<Element> set = generator.getJavaClientGeneratorElements();
		assertTrue(set.size() == 1);
		assertElement(set.iterator().next(), "javaClientGenerator", new Attribute("type", "XMLMAPPER"), 
				new Attribute("targetPackage", "dao"), new Attribute("targetProject", "/yobatis/src/main/java"));
	}
	
	@Test
	public void sqlmapGenerators() {
		Folder folder = TestUtil.mockFolder("/yobatis/src/main/resource");
		resouceFolders.add(folder);
		build();
		Set<Element> set = generator.getSqlMapGeneratorElements();
		assertTrue(set.size() == 1);
		assertElement(set.iterator().next(), "sqlMapGenerator",
				new Attribute("targetPackage", "mybatis-mappers"),
				new Attribute("targetProject", "/yobatis/src/main/resource"));
	}
	
	
	@Test
	public void javaModelGenerators() {
		build();
		Set<Element> set = generator.getJavaModelGeneratorElements();
		assertTrue(set.isEmpty());

		Folder folder = TestUtil.mockFolder("/yobatis/src/main/java/model");
		modelFolders.add(folder);
		build();
		set = generator.getJavaModelGeneratorElements();
		assertTrue(set.size() == 1);

		assertElement(set.iterator().next(), "javaModelGenerator", 
				new Attribute("targetPackage", "model"),
				new Attribute("targetProject", "/yobatis/src/main/java"));
	}
	
	
	@Test
	public void tableWithoutPk() {
		Table table = mockTable("table", null);
		tables.add(table);
		build();
		Set<Element> tables = generator.getTableElements();
		assertTrue(tables.size() == 1);
		assertElement(tables.iterator().next(), "table", new Attribute("tableName", "table"),
			new Attribute("modelType", "flat"));
		assertTrue(tables.iterator().next().elements().isEmpty());
	}
	
	@Test
	public void tableWithPk() {
		Table table = mockTable("table", "pk");
		tables.add(table);
		build();
		Set<Element> tables = generator.getTableElements();
		assertTrue(tables.size() == 1);
		assertElement(tables.iterator().next(), "table", new Attribute("tableName", "table"),
			new Attribute("modelType", "flat"));
		Element pk = tables.iterator().next().elements().get(0);
		assertElement(pk, "generatedKey", new Attribute("column", "pk"), new Attribute("sqlStatement", "mysql"));
	}
	

	//Get dao path when there is no or more than one dao folders.
	@Test(expected = InvalidMybatisGeneratorConfigException.class)
	public void getDaoPathWhenNotUnique() {
		try {
			build();
			generator.getDaoDirPath();
			fail();
		} catch (InvalidMybatisGeneratorConfigException e) {
			// Expected
		}
		daoFolders.add(TestUtil.mockFolder("/src/main/java/dao1"));
		daoFolders.add(TestUtil.mockFolder("/src/main/java/dao2"));
		build();
		generator.getDaoDirPath();
	}
	
	@Test
	public void getSingleDaoPath() {
		daoFolders.add(TestUtil.mockFolder("/src/main/java/dao1"));
		build();
		assertTrue(generator.getDaoDirPath().equals("/src/main/java/dao1"));
	}
	
	@Test
	public void javaMapperPackageName() {
		daoFolders.add(TestUtil.mockFolder("/src/main/java/dao1"));
		build();
		assertTrue(generator.getPackageNameOfJavaMappers().equals("dao1"));
	}
	
	@Test
	public void criteriaDirPath() {
		modelFolders.add(TestUtil.mockFolder("/src/main/java/hello/model"));
		build();
		assertTrue(generator.getCriteriaDirPath().equals("/src/main/java/hello/model/criteria"));
		assertTrue(generator.getDomainDirPath().equals("/src/main/java/hello/model"));
		assertTrue(generator.getPackageNameOfDomains().equals("hello.model"));
	}
	
	@Test
	public void mapperPath() {
		resouceFolders.add(TestUtil.mockFolder("/src/main/java/resource"));
		build();
		assertTrue(generator.getXmlMapperDirPath().equals("/src/main/java/resource/mybatis-mappers"));
	}

}
