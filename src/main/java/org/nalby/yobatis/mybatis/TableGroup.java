package org.nalby.yobatis.mybatis;

import java.util.LinkedList;
import java.util.List;

import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;

public final class TableGroup {
	//private String modelPackageName;
	private List<Table> tables;
	
	private Folder folder;
	
	public TableGroup(Folder folder) {
		this.tables = new LinkedList<>();
		this.folder = folder;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void addTable(Table table) {
		tables.add(table);
	}
	
	public Folder getFolder() {
		return folder;
	}
}
