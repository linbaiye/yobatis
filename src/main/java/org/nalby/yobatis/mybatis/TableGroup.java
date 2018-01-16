package org.nalby.yobatis.mybatis;

import java.util.LinkedList;
import java.util.List;

import org.nalby.yobatis.sql.Table;

public final class TableGroup {
	
	private String modelPackageName;
	
	private List<Table> tables;
	
	public TableGroup(String packageName) {
		this.modelPackageName = packageName;
		this.tables = new LinkedList<>();
	}

	public String getPackageName() {
		return modelPackageName;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void addTable(Table table) {
		tables.add(table);
	}

}
