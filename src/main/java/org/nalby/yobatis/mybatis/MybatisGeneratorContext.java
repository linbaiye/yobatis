package org.nalby.yobatis.mybatis;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;

public class MybatisGeneratorContext  {
	
	private DocumentFactory factory = DocumentFactory.getInstance();

	private Element context;
	
	private Element javaClient;

	private Element javaModel;

	private Element xmlMapper;
	
	private List<Element> tableElements;
	
	
	public MybatisGeneratorContext(String id, DatabaseMetadataProvider databaseMetadataProvider) {
		if (id == null) {
			id = "";
		}
		context = factory.createElement("context");
		context.addAttribute("id", id);
		context.addAttribute("targetRuntime", MybatisGeneratorAnalyzer.TARGET_RUNTIME);
		appendYobatisPlugin(context);
		appendCriteriaPlugin(context);
		appendJdbcConnection(context, databaseMetadataProvider);
		appendTypeResolver(context);
	}
	
	private void appendTypeResolver(Element context) {
		Element typeResolver = context.addElement("javaTypeResolver");
		Element property = typeResolver.addElement("property");
		property.addAttribute("name", "forceBigDecimals");
		property.addAttribute("value", "false");
	}
	
	private void appendJdbcConnection(Element context, DatabaseMetadataProvider sql) {
		Element jdbConnection = context.addElement("jdbcConnection");
		jdbConnection.addAttribute("driverClass", sql.getDriverClassName());
		jdbConnection.addAttribute("connectionURL", sql.getUrl());
		jdbConnection.addAttribute("userId", sql.getUsername());
		jdbConnection.addAttribute("password", sql.getPassword());
	}
	
	private String packageNameOfFolder(Folder folder) {
		if (folder == null) {
			return "";
		}
		String packageName = FolderUtil.extractPackageName(folder.path());
		return packageName == null ? "" : packageName;
	}
	
	private String sourceCodePath(Folder folder) {
		if (folder == null) {
			return "";
		}
		return FolderUtil.wipePackagePath(folder.path());
	}
	
	private void appendCriteriaPlugin(Element context) {
		Element criteriaPluginElement = context.addElement("plugin");
		criteriaPluginElement.addAttribute("type",  MybatisGeneratorAnalyzer.YOBATIS_CRITERIA_PLUGIN);
	}
	
	private void appendYobatisPlugin(Element context) {
		Element pluginElement = context.addElement("plugin");
		pluginElement.addAttribute("type",  MybatisGeneratorAnalyzer.YOBATIS_PLUGIN);
		Element property = pluginElement.addElement("property");
		property.addAttribute("name", "enableBaseClass");
		property.addAttribute("value", "true");
		property = pluginElement.addElement("property");
		property.addAttribute("name", "enableToString");
		property.addAttribute("value", "true");
	}
	
	public void appendJavaModelGenerator(Folder folder) {
		javaModel = factory.createElement(MybatisGeneratorAnalyzer.MODEL_GENERATOR_TAG);
		javaModel.addAttribute("targetPackage", packageNameOfFolder(folder));
		javaModel.addAttribute("targetProject", sourceCodePath(folder));
	}

	public void appendSqlMapGenerator(Folder folder) {
		xmlMapper = factory.createElement(MybatisGeneratorAnalyzer.SQLMAP_GENERATOR_TAG);
		xmlMapper.addAttribute("targetPackage", "mybatis-mappers");
		xmlMapper.addAttribute("targetProject", folder == null? "" : folder.path());
	}
	
	public void appendJavaClientGenerator(Folder folder) {
		javaClient = factory.createElement(MybatisGeneratorAnalyzer.CLIENT_GENERATOR_TAG);
		javaClient.addAttribute("type", "XMLMAPPER");
		javaClient.addAttribute("targetPackage", packageNameOfFolder(folder));
		javaClient.addAttribute("targetProject", sourceCodePath(folder));
	}
	
	public void appendTables(List<Table> tables, String schema)  {
		Expect.notNull(tables, "tables must not be null.");
		tableElements = new LinkedList<>();
		for (Table table: tables) {
			Element element = factory.createElement("table");
			element.addAttribute("tableName", table.getName());
			element.addAttribute("schema", schema == null ? "" : schema);
			element.addAttribute("modelType", "flat");
			String autoIncKey = table.getAutoIncPK();
			if (autoIncKey != null) {
				Element pk = element.addElement("generatedKey");
				pk.addAttribute("column", autoIncKey);
				pk.addAttribute("sqlStatement", "mysql");
				pk.addAttribute("identity", "true");
			}
			tableElements.add(element);
		}
	}
	
	public Element getContext() {
		if (javaModel != null) {
			context.add(javaModel);
		}
		if (xmlMapper != null) {
			context.add(xmlMapper);
		}
		if (javaClient != null) {
			context.add(javaClient);
		}
		if (tableElements != null) {
			for (Element e : tableElements) {
				context.add(e);
			}
		}
		return context;
	}

}
