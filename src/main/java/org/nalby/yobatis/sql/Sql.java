package org.nalby.yobatis.sql;

import java.util.List;

public interface Sql {
	
	public List<String> getTableNames();
	
	public String getSchema();

}
