package org.nalby.yobatis.sql;

import java.util.ArrayList;
import java.util.List;

public class Table {

	private String name;

	private List<String> autoIncColumn;
	
	private List<String> primaryKey;

	public Table(String name) {
		this.name = name;
		autoIncColumn = new ArrayList<>();
		primaryKey = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addAutoIncColumn(String autoIncKey) {
		this.autoIncColumn.add(autoIncKey);
	}

	public List<String> getAutoIncColumn() {
		return autoIncColumn;
	}

	public List<String> getPrimaryKey() {
		return primaryKey;
	}

	public void addPrimaryKey(String primaryKey) {
		this.primaryKey.add(primaryKey);
	}
	
	public String getAutoIncPK() {
		if (primaryKey.size() != 1 || autoIncColumn.isEmpty()) {
			return null;
		}
		String tmp = primaryKey.get(0);
		return autoIncColumn.contains(tmp) ? tmp : null;
	}
	
}
