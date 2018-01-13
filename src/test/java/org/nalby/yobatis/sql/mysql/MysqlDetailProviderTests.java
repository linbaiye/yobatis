package org.nalby.yobatis.sql.mysql;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.attribute.AclEntry.Builder;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

@PrepareForTest(DriverManager.class)
@RunWith(PowerMockRunner.class)
public class MysqlDetailProviderTests {
	
	private MysqlDatabaseDetailProvider provider;
	
	private String username = "username";
	private String password = "password";
	private String url = "jdbc:mysql://localhost:3306/yobatis";
	private String driver = "driver";
	private String jarPath = "mysql.jar";
	
	private void buildInstance() {
		Constructor<?>[] constructors = MysqlDatabaseDetailProvider.class.getDeclaredConstructors();
		for (Constructor<?> constructor : constructors) {
			if (constructor.getParameterTypes().length == 5) {
				constructor.setAccessible(true);
				try {
					provider = (MysqlDatabaseDetailProvider)constructor.newInstance(username, password, url, driver, jarPath);
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
		PowerMockito.mockStatic(DriverManager.class);
        BDDMockito.given(DriverManager.getConnection(anyString(), any())).willReturn(null);
        BDDMockito.given(DriverManager.getConnection(anyString())).willReturn(null);
        BDDMockito.given(DriverManager.getConnection(anyString(), anyString(), anyString())).willReturn(null);
        buildInstance();
	}
	
	

	
	
	@Test
	public void test() throws SQLException {
		assertTrue(provider.getSchema().equals("yobatis"));
	}

}
