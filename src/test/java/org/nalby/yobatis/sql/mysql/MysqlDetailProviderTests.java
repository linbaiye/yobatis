package org.nalby.yobatis.sql.mysql;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@PrepareForTest({DriverManager.class, MysqlDatabaseMetadataProvider.class})
@RunWith(PowerMockRunner.class)
public class MysqlDetailProviderTests {
	
	private MysqlDatabaseMetadataProvider provider;
	
	
	private String username = "username";
	private String password = "password";
	private String url = "jdbc:mysql://localhost:3306/yobatis";
	private String driver = "driver";
	private String jarPath = "mysql.jar";
	
	private Connection connection;
	
	private DatabaseMetaData metaData;
	
	private ResultSet resultSet;

	private void buildInstance() {
		Constructor<?>[] constructors = MysqlDatabaseMetadataProvider.class.getDeclaredConstructors();
		for (Constructor<?> constructor : constructors) {
			if (constructor.getParameterTypes().length == 5) {
				constructor.setAccessible(true);
				try {
					provider = (MysqlDatabaseMetadataProvider)constructor.newInstance(username, password, url, driver, jarPath);
					return;
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
	}
	
	@Before
	public void setup() throws SQLException {
        connection = mock(Connection.class);
		PowerMockito.mockStatic(DriverManager.class);
        BDDMockito.given((DriverManager.getConnection(any(), any()))).willReturn(connection);
        BDDMockito.given(DriverManager.getConnection(anyString())).willReturn(connection);
        BDDMockito.given(DriverManager.getConnection(anyString(), anyString(), anyString())).willReturn(connection);
    	username = "username";
    	password = "password";
    	url = "jdbc:mysql://localhost:3306/yobatis";
    	driver = "driver";
    	jarPath = "mysql.jar";
        metaData = PowerMockito.mock(DatabaseMetaData.class);
        resultSet = PowerMockito.mock(ResultSet.class);
        BDDMockito.given(connection.getMetaData()).willReturn(metaData);
        buildInstance();
	}
	
	
	@Test
	public void crendentials() throws SQLException {
		assertTrue(provider.getSchema().equals("yobatis"));
		assertTrue(provider.getPassword().equals(password));
		assertTrue(provider.getUrl().equals(url));
		assertTrue(provider.getConnectorJarPath().equals(jarPath));
		assertTrue(provider.getDriverClassName().equals(driver));
	}
	
	@Test
	public void emtpyTables() throws SQLException {
		BDDMockito.given(resultSet.next()).willReturn(false);
		BDDMockito.given(metaData.getTables(anyString(), anyString(), anyString(), any(String[].class)))
		.willReturn(resultSet);
		assertTrue(provider.getTables().isEmpty());
	}

}
