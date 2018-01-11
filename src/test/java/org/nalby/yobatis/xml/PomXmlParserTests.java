package org.nalby.yobatis.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import org.dom4j.DocumentException;
import org.junit.Test;

public class PomXmlParserTests {
	
	@Test
	public void testNoVersionConfigured() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "</dependency>"
				+ "</dependencies></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue(null == tmp);
		
		xml = "<project><artifactId>test</artifactId><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>${mysql.version}</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue(null == tmp);
	}
	
	@Test
	public void testDirectConfiguredVersion() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><dependencies>"
				+ "<dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>5.4.9</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue("mysql/mysql-connector-java/5.4.9/mysql-connector-java-5.4.9.jar".equals(tmp));
	}

	@Test
	public void testVersionVariable() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><properties>"
				+ "<mysql.version>5.4.1</mysql.version></properties>"
				+ "<dependencies><dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>${mysql.version}</version>"
				+ "</dependency>"
				+ "</dependencies></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue("mysql/mysql-connector-java/5.4.1/mysql-connector-java-5.4.1.jar".equals(tmp));
	}
	
	@Test
	public void testDependencyManagement() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><properties>"
				+ "<mysql.version>5.4.1</mysql.version></properties>"
				+ "<dependencyManagement><dependencies><dependency>"
				+ "<groupId>mysql</groupId>"
				+ "<artifactId>mysql-connector-java</artifactId>"
				+ "<version>${mysql.version}</version>"
				+ "</dependency>"
				+ "</dependencies></dependencyManagement></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		String tmp = parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver");
		assertTrue("mysql/mysql-connector-java/5.4.1/mysql-connector-java-5.4.1.jar".equals(tmp));
	}
	

	@Test
	public void testNoProfileProperty() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><profiles><profile><id>develop</id><activation><activeByDefault>true</activeByDefault>"
				+ "</activation><properties><uplending.jdbc.datasource.type></uplending.jdbc.datasource.type>" + 
		    "</properties></profile></profiles></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(null == parser.getProperty("uplending.jdbc.datasource.type"));
	}
	
	@Test
	public void testProfileProperty() throws DocumentException, IOException {
		String xml = "<project><artifactId>test</artifactId><profiles><profile><id>develop</id><activation><activeByDefault>true</activeByDefault>"
				+ "</activation><properties><uplending.jdbc.datasource.type>test</uplending.jdbc.datasource.type>"
				+ "<type>test</type>" + 
		    "</properties></profile></profiles></project>";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue("test".equals(parser.getProperty("uplending.jdbc.datasource.type")));
		assertTrue("test".equals(parser.getProperty("type")));
	}
	
	@Test
	public void resourceDirsWithoutPackaging() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"    <modelVersion>4.0.0</modelVersion>\n" + 
				"    <groupId>test</groupId>\n" + 
				"    <artifactId>test</artifactId>\n" + 
				"    <version>1.3.0</version>\n" + 
				"    <name>test</name>\n" + 
				"    <url>test</url>\n" + 
				"    <build>\n" + 
				"      <resources>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/resources</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/conf/${profiles.active}</directory>\n" + 
				"        </resource>\n" + 
				"      </resources>\n" + 
				"    </build>\n" + 
				"</project>"; 
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> dirs = parser.getResourceDirs();
		assertTrue(dirs.isEmpty());
	}
	
	private void assertStringsInSet(Set<String> set, String ... list) {
		for (String str: list) {
			assertTrue(set.contains(str));
		}
	}
	
	@Test
	public void resourceDirsWithoutPlaceholder() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"    <modelVersion>4.0.0</modelVersion>\n" + 
				"    <groupId>test</groupId>\n" + 
				"    <artifactId>test</artifactId>\n" + 
				"    <version>1.3.0</version>\n" + 
				"    <packaging>war</packaging>\n" + 
				"    <name>test</name>\n" + 
				"    <url>test</url>\n" + 
				"    <build>\n" + 
				"      <resources>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/resources</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/conf</directory>\n" + 
				"        </resource>\n" + 
				"      </resources>\n" + 
				"    </build>\n" + 
				"</project>"; 
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> dirs = parser.getResourceDirs();
		assertStringsInSet(dirs, "src/main/conf", "src/main/resources");
	}
	
	@Test
	public void resourceDirsWithSolvablePlaceholder() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"    <modelVersion>4.0.0</modelVersion>\n" + 
				"    <groupId>test</groupId>\n" + 
				"    <artifactId>test</artifactId>\n" + 
				"    <version>1.3.0</version>\n" + 
				"    <packaging>war</packaging>\n" + 
				"    <properties>\n" + 
				"		<dir>test</dir>\n" + 
				"		<dir2>dirx</dir2>\n" + 
				"		<dir1>src/test</dir1>\n" + 
				"	 </properties>\n" +
				"    <name>test</name>\n" + 
				"    <url>test</url>\n" + 
				"    <build>\n" + 
				"      <resources>\n" + 
				"        <resource>\n" + 
				"            <directory>${dir1}</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/${dir}</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/${dir2}/xxx</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/${dir}${dir1}</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/${dir}/${dir}</directory>\n" + 
				"        </resource>\n" + 
				"      </resources>\n" + 
				"    </build>\n" + 
				"</project>"; 
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> dirs = parser.getResourceDirs();
		assertStringsInSet(dirs, "src/main/dirx/xxx", "src/test", "src/main/test",
				"src/main/resources", "src/main/testsrc/test", "src/test/test");
	}
	
	//Profile properties must take precedence over normal properties.
	@Test
	public void propertyPrecendence() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <packaging>war</packaging>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"  <properties>\n" + 
				"    <test>hello</test>\n" + 
				"  </properties>\n" + 
				"  <build>\n" + 
				"    <resources>\n" + 
				"      <resource>\n" + 
				"        <directory>src/main/resources</directory>\n" + 
				"      </resource>\n" + 
				"      <resource>\n" + 
				"        <directory>src/main/${test}/conf/</directory>\n" + 
				"      </resource>\n" + 
				"    </resources>\n" + 
				"  </build>\n" + 
				"  <profiles>\n" + 
				"    <profile>\n" + 
				"      <id>development</id>\n" + 
				"      <properties>\n" + 
				"        <test>dev</test>\n" + 
				"      </properties>\n" + 
				"      <activation>\n" + 
				"        <activeByDefault>true</activeByDefault>\n" + 
				"      </activation>\n" + 
				"    </profile>\n" + 
				"    <profile>\n" + 
				"      <id>production</id>\n" + 
				"      <properties>\n" + 
				"        <profiles.active>production</profiles.active>\n" + 
				"      </properties>\n" + 
				"    </profile>\n" + 
				"  </profiles>\n" + 
				"</project>\n";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue("dev".equals(parser.getProperty("test")));
	}
	
	
	//Drop unsolvable placehodler.
	@Test
	public void resourceDirsWithUnsolvablePlaceholder() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"    <modelVersion>4.0.0</modelVersion>\n" + 
				"    <groupId>test</groupId>\n" + 
				"    <artifactId>test</artifactId>\n" + 
				"    <version>1.3.0</version>\n" + 
				"    <packaging>war</packaging>\n" + 
				"    <properties>\n" + 
				"		<dir>test</dir>\n" + 
				"		<dir1>src/test</dir1>\n" + 
				"	 </properties>\n" +
				"    <name>test</name>\n" + 
				"    <url>test</url>\n" + 
				"    <build>\n" + 
				"      <resources>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/${dir}</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/main/resources</directory>\n" + 
				"        </resource>\n" + 
				"        <resource>\n" + 
				"            <directory>src/xxx/${dir2}/xxx</directory>\n" + 
				"        </resource>\n" + 
				"      </resources>\n" + 
				"    </build>\n" + 
				"</project>"; 
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		Set<String> dirs = parser.getResourceDirs();
		assertTrue(dirs.size() == 2);
		assertStringsInSet(dirs, "src/main/test", "src/main/resources");
	}
	
	@Test
	public void noModule() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <packaging>war</packaging>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"  <modules>\n" + 
				"  </modules>\n" + 
				"</project>\n";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.getModuleNames().isEmpty());
	}
	
	@Test
	public void modules() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <packaging>war</packaging>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"  <modules>\n" + 
				"    <module>hello</module>\n" + 
				"    <module>world</module>\n" + 
				"  </modules>\n" + 
				"</project>\n" + 
				"";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertStringsInSet(parser.getModuleNames(), "hello", "world");
	}
	
	
	@Test
	public void isContainer() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"</project>\n";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.isContainer());

		xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <packaging></packaging>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"</project>\n";
		parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertTrue(parser.isContainer());
	}
	
	@Test
	public void isNotContainer() throws DocumentException, IOException {
		String xml = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>test</groupId>\n" + 
				"  <artifactId>test</artifactId>\n" + 
				"  <packaging>war</packaging>\n" + 
				"  <version>1.3.0</version>\n" + 
				"  <name>test</name>\n" + 
				"  <url>test</url>\n" + 
				"</project>\n";
		PomXmlParser parser = new PomXmlParser(new ByteArrayInputStream(xml.getBytes()));
		assertFalse(parser.isContainer());
	}
	
	
}
