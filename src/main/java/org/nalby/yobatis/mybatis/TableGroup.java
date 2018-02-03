/*
 *    Copyright 2018 the original author or authors.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *    use this file except in compliance with the License.  You may obtain a copy
 *    of the License at
 *    
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *    License for the specific language governing permissions and limitations under
 *    the License.
 */
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
