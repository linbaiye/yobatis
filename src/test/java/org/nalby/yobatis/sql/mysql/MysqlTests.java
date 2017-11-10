package org.nalby.yobatis.sql.mysql;

import static org.junit.Assert.*;

import org.junit.Test;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.mysql.Mysql.Builder;

public class MysqlTests {
	
	@Test
	public void getSchema() {
		Builder builder = Mysql.builder();
		builder.setConnectorJarPath("test");
		builder.setUrl("jdbc:mysql://test.com/hello");
		builder.setDriverClassName("test");
		builder.setPassword("pass");
		builder.setUsername("test");
		Sql sql = builder.build();
		assertTrue(sql.getSchema().equals("hello"));
	}

}
