package org.nalby.yobatis.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.naming.Context;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.mockito.cglib.transform.impl.AddDelegateTransformer;
import org.nalby.yobatis.exception.ProjectException;

public class MybatisGeneratorConfigGenerator {
	
	private Project project;
	
	private Document document;

	DocumentFactory factory = DocumentFactory.getInstance();
	
	public MybatisGeneratorConfigGenerator(Project project) {
		this.project = project;
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

	
	private void appendJdbcConnection(Element root) {
		Element jdbConnection = factory.createElement("jdbcConnection");
		jdbConnection.addAttribute("driverClass", project.getDatabaseDriverClassName());
		jdbConnection.addAttribute("connectionURL", project.getDatabaseUrl());
		jdbConnection.addAttribute("userId", project.getDatabaseUsername());
		jdbConnection.addAttribute("password", project.getDatabasePassword());
		root.add(jdbConnection);
	}

	private void appendJavaModelGenerator(Element root) {
		Element element = factory.createElement("JavaModelGenerator");
		element.addAttribute("targetPackage", project.getDatabaseConnectorPath());
	}
	
	private Element appendContext(Element root) {
		Element context = factory.createElement("context");
		context.addAttribute("id", "DB2Tables");
		context.addAttribute("targetRuntime", "MyBatis3");
		root.add(context);
		return context;
	}
	
	public void generate() {
		document = factory.createDocument();
		DocumentType type = factory.createDocType("generatorConfiguration", "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN", "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd");
		document.setDocType(type);
		Element root = factory.createElement("generatorConfiguration");
		document.setRootElement(root);
		appendClassPathEntry(root);

		Element context = appendContext(root);
		appendJdbcConnection(context);
		appendTypeResolver(context);
		try {
			String content = convertToString();
			project.wirteGeneratorConfigFile("mybatisGeneratorConfig.xml", content);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProjectException(e);
		}
	}
	
}
