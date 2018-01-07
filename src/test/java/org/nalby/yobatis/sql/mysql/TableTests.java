package org.nalby.yobatis.sql.mysql;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.sql.Table;

public class TableTests {
	
	private Table table;
	
	@Before
	public void setup() {
		table = new Table("test");
	}

	@Test
	public void hasNoKey() {
		assertNull(table.getAutoIncPK());
	}
	
	@Test
	public void hasCompoundKeys() {
		table.addAutoIncColumn("f1");
		table.addPrimaryKey("f1");
		table.addPrimaryKey("f2");
		assertNull(table.getAutoIncPK());
	}

	@Test
	public void hasNoAutoIncColumn() {
		table.addPrimaryKey("f1");
		assertNull(table.getAutoIncPK());
	}
	
	@Test
	public void hasAutoincKey() {
		table.addPrimaryKey("f1");
		table.addAutoIncColumn("f1");
		table.addAutoIncColumn("f2");
		assertTrue("f1".equals(table.getAutoIncPK()));
	}

}
