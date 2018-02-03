package org.nalby.yobatis.mybatis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.mybatis.TableGroup;
import org.nalby.yobatis.mybatis.TokenSimilarityTableGrouper;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.TestUtil;

public class TokenSimilarityGrouperTests {
	
	private TokenSimilarityTableGrouper grouper;
	
	private List<Folder> folders;
	
	private List<Table> tables;
	
	private static final String DEFAULT_FOLDER_PATH = "/src/main/java/yobatis/model";
	
	private Folder defaultFolder;
	
	private List<TableGroup> result;

	@Before
	public void setup() {
		folders = new ArrayList<>();
		defaultFolder = TestUtil.mockFolder(DEFAULT_FOLDER_PATH);
		folders.add(defaultFolder);
		grouper = new TokenSimilarityTableGrouper(folders);
		tables = new ArrayList<>();
	}
	
	private void addTable(String name) {
		tables.add(new Table(name));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void emtpyFolders() {
		try {
			new TokenSimilarityTableGrouper(null);
			fail();
		} catch (IllegalArgumentException e) {

		}
		try {
			folders.clear();
			new TokenSimilarityTableGrouper(folders);
			fail();
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
		}
		folders.add(TestUtil.mockFolder("/src"));
		new TokenSimilarityTableGrouper(folders);
	}
	
	
	private void assertTables(List<Table> tables, String ... names) {
		assertTrue(tables.size() == names.length);
		for (Table table : tables) {
			boolean found = false;
			for (String name : names) {
				if (table.getName().equals(name)) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}
	
	private void assertGroups(List<TableGroup> groups, Folder ... folders) {
		assertTrue(groups.size() == folders.length);
		for (TableGroup group: groups) {
			boolean found = false;
			for (Folder folder: folders) {
				if (group.getFolder() == folder) {
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
	}
	
	
	@FunctionalInterface
	private interface NameMatcher<T> {
		boolean match(T item, String name);
	}
	
	

	@Test
	public void singleFolder() {
		addTable("table1");
		addTable("table2");
		result = grouper.group(tables);
		assertTrue(result.size() == 1);
		TableGroup group = result.get(0);
		assertTrue(group.getFolder() == defaultFolder);
		assertTrue(folders.get(0) == group.getFolder());
		assertTables(group.getTables(), "table1", "table2");
	}
	

	@Test
	public void twoFolders() {
		Folder adminFolder = TestUtil.mockFolder("/src/main/java/admin/model");
		folders.add(adminFolder);
		grouper = new TokenSimilarityTableGrouper(folders);
		addTable("sys_admin");
		addTable("yobatis_user");
		result = grouper.group(tables);
		assertGroups(result, adminFolder, defaultFolder);
		for (TableGroup group : result) {
			if (group.getFolder() == defaultFolder) {
				assertTables(group.getTables(), "yobatis_user");
			} else {
				assertTables(group.getTables(), "sys_admin");
			}
		}
	}

}
