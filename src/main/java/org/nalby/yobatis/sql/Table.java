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

	public void addAutoIncColumn(String autoIncKey) {
		this.autoIncColumn.add(autoIncKey);
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
