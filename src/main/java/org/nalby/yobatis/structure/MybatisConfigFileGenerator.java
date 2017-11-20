package org.nalby.yobatis.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.xml.MybatisXmlParser;

public class MybatisConfigFileGenerator {
	private Project project;
	private Document document;
	private Sql sql;
	private ErrorCode errorCode;

	private enum ErrorCode {
		OK,
		NO_MODEL_PATH,
		NO_DAO_PATH,
		NO_RESOURCES_PATH,
		MULTIPLE_MODEL_PATHS,
		MULTIPLE_DAO_PATHS,
		MULTIPLE_RESOURCES_PATHS,
		NO_TABLES
	}
	
	public final static String CONFIG_FILENAME = "mybatisGeneratorConfig.xml";
	
	private DocumentFactory factory = DocumentFactory.getInstance();

	private Element root;

	private Element classPathEntry;
	
	private Element context;
	
	private Element jdbConnection;
	
	private Element typeResolver;

	private Set<Element> javaModelGenerators = new HashSet<Element>();

	private Set<Element> sqlMapGenerators = new HashSet<Element>();
	
	private Set<Element> javaClientGenerators = new HashSet<Element>();

	private Set<Element> tables = new HashSet<Element>();
	
	public MybatisConfigFileGenerator(Project project, Sql sql) {
		this.project = project;
		this.sql = sql;
		errorCode = ErrorCode.OK;
		createDocument();
		root = factory.createElement("generatorConfiguration");
		document.setRootElement(root);
		appendClassPathEntry(root);
		context = appendContext(root);
		appendJdbcConnection(context);
		appendTypeResolver(context);
		appendJavaModelGenerator(context);
		appendSqlMapGenerator(context);
		appendJavaClientGenerator(context);
		appendTables(context);
	}

	public Element getClassPathEntryElement() {
		return classPathEntry;
	}
	
	public Element getJdbConnectionElement() {
		return jdbConnection;
	}
	
	public Element getJavaTypeResolverElement() {
		return typeResolver;
	}
	
	public Set<Element> getJavaModelGeneratorElements() {
		return javaModelGenerators;
	}
	
	public Set<Element> getSqlMapGeneratorElements() {
		return sqlMapGenerators;
	}
	
	public Set<Element> getJavaClientGeneratorElements() {
		return javaClientGenerators;
	}
	
	public Set<Element> getTableElements() {
		return tables;
	}
	
	public Element getContext() {
		return context;
	}
	
	private void appendClassPathEntry(Element root) {
		classPathEntry = root.addElement(MybatisXmlParser.CLASS_PATH_ENTRY_TAG);
		classPathEntry.addAttribute("location", sql.getConnectorJarPath());
	}
	
	private void appendTypeResolver(Element root) {
		typeResolver = root.addElement("javaTypeResolver");
		Element property = typeResolver.addElement("property");
		property.addAttribute("name", "forceBigDecimals");
		property.addAttribute("value", "false");
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
		if (names.isEmpty()) {
			errorCode = ErrorCode.NO_TABLES;
		}
		for (String name: names) {
			Element table = context.addElement("table");
			table.addAttribute("tableName", name);
			table.addAttribute("schema", sql.getSchema());
			tables.add(table);
		}
	}
	
	private void appendJdbcConnection(Element root) {
		jdbConnection = root.addElement("jdbcConnection");
		jdbConnection.addAttribute("driverClass", sql.getDriverClassName());
		jdbConnection.addAttribute("connectionURL", sql.getUrl());
		jdbConnection.addAttribute("userId", sql.getUsername());
		jdbConnection.addAttribute("password", sql.getPassword());
	}

	private void appendJavaModelGenerator(Element context) {
		List<String> paths = project.getSyspathsOfModel();
		if (paths.isEmpty()) {
			errorCode = ErrorCode.NO_MODEL_PATH;
			paths.add(project.getFullPath());
		} else if (paths.size() > 1) {
			errorCode = ErrorCode.MULTIPLE_MODEL_PATHS;
		}
		for (String path: paths) {
			String packageName = project.getPackageName(path);
			Element javaModelGenerator = context.addElement("javaModelGenerator");
			javaModelGenerator.addAttribute("targetPackage", packageName == null? "": packageName);
			javaModelGenerator.addAttribute("targetProject", path);
			javaModelGenerators.add(javaModelGenerator);
		}
	}
	
	private Element appendContext(Element root) {
		context = root.addElement("context");
		context.addAttribute("id", MybatisXmlParser.CONTEXT_ID);
		context.addAttribute("targetRuntime", MybatisXmlParser.TARGET_RUNTIME);
		return context;
	}
	
	private void appendSqlMapGenerator(Element context) {
		List<String> paths = project.getSyspathsOfResources();
		if (paths.isEmpty()) {
			errorCode = ErrorCode.NO_RESOURCES_PATH;
			paths.add(project.getFullPath());
		} else if (paths.size() > 1) {
			errorCode = ErrorCode.MULTIPLE_RESOURCES_PATHS;
		}
		for (String path: paths) {
			Element generator = context.addElement("sqlMapGenerator");
			generator.addAttribute("targetPackage", "mybatis-mappers/autogen");
			generator.addAttribute("targetProject", path);
			sqlMapGenerators.add(generator);
		}
	}
	
	private void appendJavaClientGenerator(Element context) {
		List<String> paths = project.getSyspathsOfDao();
		if (paths.isEmpty()) {
			errorCode = ErrorCode.NO_DAO_PATH;
			paths.add(project.getFullPath());
		} else if (paths.size() > 1) {
			errorCode = ErrorCode.MULTIPLE_MODEL_PATHS;
		}
		for (String path: paths) {
			String packageName = project.getPackageName(path);
			Element generator = context.addElement("javaClientGenerator");
			generator.addAttribute("type", "XMLMAPPER");
			generator.addAttribute("targetPackage", packageName == null ? "" : packageName);
			generator.addAttribute("targetProject", path);
			javaClientGenerators.add(generator);
		}
	}
	
	public boolean hasError() {
		return errorCode != ErrorCode.OK;
	}
	
	private void createDocument() {
		document = factory.createDocument();
		DocumentType type = factory.createDocType("generatorConfiguration", "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN", "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd");
		document.setDocType(type);
	}
	
	public String getXmlConfig() {
		try {
			return convertToString();
		} catch (IOException e) {
			throw new ProjectException(e);
		}
	}
	
}
