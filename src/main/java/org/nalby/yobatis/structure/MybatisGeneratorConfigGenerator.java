package org.nalby.yobatis.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.sql.Sql;

public class MybatisGeneratorConfigGenerator {
	
	private Project project;
	
	private Document document;
	
	private Sql sql;

	DocumentFactory factory = DocumentFactory.getInstance();
	
	public MybatisGeneratorConfigGenerator(Project project, Sql sql) {
		this.project = project;
		this.sql = sql;
	}
	
	private void appendClassPathEntry(Element root) {
		Element classPathEntry = factory.createElement("classPathEntry");
		classPathEntry.addAttribute("location", project.getDatabaseConnectorFullPath());
		root.add(classPathEntry);
	}
	
	private void appendTypeResolver(Element root) {
		Element typeResolver = factory.createElement("javaTypeResolver");
		Element property = factory.createElement("property");
		property.addAttribute("name", "forceBigDecimals");
		property.addAttribute("value", "false");
		typeResolver.add(property);
		root.add(typeResolver);
	}
	
	private String convertToString() throws IOException {
	    OutputFormat format = OutputFormat.createPrettyPrint();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos, true, "utf-8");
        XMLWriter writer = new XMLWriter(ps, format);
        writer.write(document);
	    String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
	    ps.close();
	    return content;
	}
	
	private void appendTables(Element context)  {
		List<String> names = sql.getTableNames();
		for (String name: names) {
			Element table = factory.createElement("table");
			table.addAttribute("tableName", name);
			table.addAttribute("schema", sql.getSchema());
			Element columnRenamingRule = factory.createElement("columnRenamingRule");
			columnRenamingRule.addAttribute("searchString", "_");
			columnRenamingRule.addAttribute("replaceString", "");
			table.add(columnRenamingRule);
			context.add(table);
		}
	}

	
	private void appendJdbcConnection(Element root) {
		Element jdbConnection = factory.createElement("jdbcConnection");
		jdbConnection.addAttribute("driverClass", project.getDatabaseDriverClassName());
		jdbConnection.addAttribute("connectionURL", project.getDatabaseUrl());
		jdbConnection.addAttribute("userId", project.getDatabaseUsername());
		jdbConnection.addAttribute("password", project.getDatabasePassword());
		root.add(jdbConnection);
	}

	private void appendJavaModelGenerator(Element context) {
		Element javaModelGenerator = factory.createElement("JavaModelGenerator");
		String path = project.getModelLayerPath();
		javaModelGenerator.addAttribute("targetPackage", path == null ? "" : path);
		path = project.getSourceCodeDirPath();
		javaModelGenerator.addAttribute("targetProject", path == null ? "" : path);
		context.add(javaModelGenerator);
	}
	
	private Element appendContext(Element root) {
		Element context = factory.createElement("context");
		context.addAttribute("id", "mysqlTables");
		context.addAttribute("targetRuntime", "MyBatis3");
		root.add(context);
		return context;
	}
	
	private void createDocument() {
		document = factory.createDocument();
		DocumentType type = factory.createDocType("generatorConfiguration", "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN", "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd");
		document.setDocType(type);
	}
	
	public void generate() {
		createDocument();

		Element root = factory.createElement("generatorConfiguration");
		appendClassPathEntry(root);
		document.setRootElement(root);

		Element context = appendContext(root);
		appendJdbcConnection(context);
		appendTypeResolver(context);
		appendJavaModelGenerator(context);
		appendTables(context);
		try {
			String content = convertToString();
			project.wirteGeneratorConfigFile("mybatisGeneratorConfig.xml", content);
		} catch (Exception e) {
			throw new ProjectException(e);
		}
	}
	
}
