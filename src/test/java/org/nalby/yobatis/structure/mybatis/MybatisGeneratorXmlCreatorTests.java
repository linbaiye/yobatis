package org.nalby.yobatis.structure.mybatis;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlCreator;
import org.nalby.yobatis.mybatis.TableGroup;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.PomTree;
import org.nalby.yobatis.util.TestUtil;

public class MybatisGeneratorXmlCreatorTests {
	
	private PomTree mockedPomTree;
	
	private DatabaseMetadataProvider mockedSql;
	
	private List<TableGroup> groups;
	
	private MybatisGeneratorXmlCreator creator;
	
	@Before
	public void setup() {
		mockedSql = mock(DatabaseMetadataProvider.class);
		when(mockedSql.getPassword()).thenReturn("password");
		when(mockedSql.getUsername()).thenReturn("username");
		when(mockedSql.getSchema()).thenReturn("schema");
		when(mockedSql.getDriverClassName()).thenReturn("driverClassName");
		when(mockedSql.getUrl()).thenReturn("url");
		when(mockedSql.getConnectorJarPath()).thenReturn("mysql.jar");

		groups = new LinkedList<>();
		mockedPomTree = mock(PomTree.class);
	}
	
	private void addTableGroup(Folder folder, String ... tableNames) {
		TableGroup group = new TableGroup(folder);
		for (String name : tableNames) {
			group.addTable(new Table(name));
		}
		groups.add(group);
		when(mockedPomTree.findMostMatchingDaoFolder(any(Folder.class))).thenReturn(folder);
		when(mockedPomTree.findMostMatchingResourceFolder(any(Folder.class))).thenReturn(folder);
	}
	
	
	private void build() {
		creator = new MybatisGeneratorXmlCreator(mockedPomTree, mockedSql, groups);
	}
	
	@Test
	public void createFromScratch() {
		Folder folder = TestUtil.mockFolder("/src/main/java/model");
		addTableGroup(folder, "table1", "table2");
		build();
		assertTrue(creator.getClassPathEntryElement().attributeValue("location").equals("mysql.jar"));
		
		assertTrue(creator.getContexts().size() == 1);
		
		assertTrue(creator.asXmlText().contains("<table"));
		assertTrue(creator.asXmlText().contains("<context"));
	}
	

}
