package org.nalby.yobatis.sql.mysql;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nalby.yobatis.exception.SqlConfigIncompleteException;
import org.nalby.yobatis.structure.Project;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MysqlTests {
	
	@Mock
	private Project mockedProject;
	
	@Test
	public void getSchemaWithoutShema() {
		when(mockedProject.getDatabaseUrl()).thenThrow(new SqlConfigIncompleteException("test"));
		Mysql mysql = new Mysql(mockedProject);
		assertTrue("".equals(mysql.getSchema()));
	}
	
	@Test
	public void getSchemaWithInvalidURL() {
		when(mockedProject.getDatabaseUrl()).thenReturn("test");
		Mysql mysql = new Mysql(mockedProject);
		assertTrue("".equals(mysql.getSchema()));
	}
	
	
	@Test
	public void getSchema() {
		Mysql mysql = new Mysql(mockedProject);
		when(mockedProject.getDatabaseUrl()).thenReturn("jdbc:mysql://localst/test");
		assertTrue("test".equals(mysql.getSchema()));

		when(mockedProject.getDatabaseUrl()).thenReturn("jdbc:mysql://localst/test?hello=1");
		assertTrue("test".equals(mysql.getSchema()));

		when(mockedProject.getDatabaseUrl()).thenReturn("jdbc:mysql://localst/test_test?hello=1");
		assertTrue("test_test".equals(mysql.getSchema()));
	}

}
