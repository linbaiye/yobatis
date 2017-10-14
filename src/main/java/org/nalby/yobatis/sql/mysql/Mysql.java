package org.nalby.yobatis.sql.mysql;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.exception.SqlConfigIncompleteException;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.structure.Project;

public class Mysql implements Sql {
	
	private Project project;
	
	private List<String> tableNames;

	public Mysql(Project project) {
		this.project = project;
		tableNames = new LinkedList<String>();
	}
	
	private Connection getConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException {
		URL url = new URL("file://" + project.getDatabaseConnectorFullPath());
		URLClassLoader classLoader = new URLClassLoader(new URL[] { url });
		Driver driver =  (Driver)Class.forName(project.getDatabaseDriverClassName(), true, classLoader).newInstance();
		DriverWrapper driverWrapper = new DriverWrapper(driver);
		DriverManager.registerDriver(driverWrapper);
		return DriverManager.getConnection(project.getDatabaseUrl(), project.getDatabaseUsername(), project.getDatabasePassword());
	}
	
	@Override
	public List<String> getTableNames() {
		tableNames.clear();
		Connection connection = null;
		try {
			connection = getConnection();
			DatabaseMetaData meta = connection.getMetaData();
			ResultSet res = meta.getTables(null, null, null, new String[] {"TABLE"});
			while (res.next()) {
				tableNames.add(res.getString("TABLE_NAME"));
			}
			res.close();
			return tableNames;
		} catch (Exception e) {
			throw new ProjectException(e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {}
		}
	}
	
	@Override
	public String getSchema() {
		try {
			Pattern pattern = Pattern.compile("jdbc:mysql://[^/]+/([^?]+).*");
			Matcher matcher = pattern.matcher(project.getDatabaseUrl());
			return matcher.find() ? matcher.group(1) : "";
		} catch (SqlConfigIncompleteException e) {
			return "";
		}

	}
	
	// See http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
	private static class DriverWrapper implements Driver {
		
		private Driver driver;
		
		public DriverWrapper(Driver driver) {
			this.driver = driver;
		}

		@Override
		public Connection connect(String url, Properties info)
				throws SQLException {
			return driver.connect(url, info);
		}

		@Override
		public boolean acceptsURL(String url) throws SQLException {
			return driver.acceptsURL(url);
		}

		@Override
		public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
				throws SQLException {
			return driver.getPropertyInfo(url, info);
		}

		@Override
		public int getMajorVersion() {
			return driver.getMajorVersion();
		}

		@Override
		public int getMinorVersion() {
			return driver.getMinorVersion();
		}

		@Override
		public boolean jdbcCompliant() {
			return driver.jdbcCompliant();
		}

		@Override
		public Logger getParentLogger() throws SQLFeatureNotSupportedException {
			return driver.getParentLogger();
		}
		
	}
}
