package org.nalby.yobatis.sql;

import java.util.List;


public abstract class DatabaseDetailProvider {
	
	protected String username;
	protected String password;
	protected String url;
	protected String connectorJarPath;
	protected String driverClassName;

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return url;
	}

	public String getConnectorJarPath() {
		return connectorJarPath;
	}

	public String getDriverClassName() {
		return driverClassName;
	}
	
	public abstract List<Table> getTables();
	
	public abstract String getSchema();

}
