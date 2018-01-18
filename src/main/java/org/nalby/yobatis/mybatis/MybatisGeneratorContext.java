package org.nalby.yobatis.mybatis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;

public class MybatisGeneratorContext {
	
	private DocumentFactory factory = DocumentFactory.getInstance();

	private Element context;
	
	private Element javaClient;

	private Element javaModel;

	private Element xmlMapper;
	
	private List<Element> tableElements;
	
	private Element typeResolver;
	
	private Element jdbConnection;
	
	private List<Element> plugins;
	
	private String id;

	public MybatisGeneratorContext(String id, DatabaseMetadataProvider databaseMetadataProvider) {
		Expect.notNull(databaseMetadataProvider, "databaseMetadataProvider must not be null.");
		Expect.notEmpty(id, "id must not be empty.");
		this.id = id;
		context = factory.createElement("context");
		context.addAttribute("id", id);
		context.addAttribute("targetRuntime", MybatisGeneratorAnalyzer.TARGET_RUNTIME);
		plugins = new ArrayList<>();
		tableElements = new LinkedList<>();
		plugins.add(createYobatisPlugin());
		plugins.add(createCriteriaPlugin());
		createJdbcConnection(databaseMetadataProvider);
		createTypeResolver();
	}
	
	
	public MybatisGeneratorContext(Element context) {
		Expect.notNull(context, "context must not be null.");
		this.id = context.attributeValue("id");
		Expect.notEmpty(id, "id must not be empty.");
		this.typeResolver = context.element("javaTypeResolver");
		this.jdbConnection = context.element("jdbcConnection");
		this.javaClient = context.element(MybatisGeneratorAnalyzer.CLIENT_GENERATOR_TAG);
		this.javaModel = context.element(MybatisGeneratorAnalyzer.MODEL_GENERATOR_TAG);
		this.xmlMapper = context.element(MybatisGeneratorAnalyzer.SQLMAP_GENERATOR_TAG);
		loadPlugins(context);
		tableElements = context.elements(MybatisGeneratorAnalyzer.TABLE_TAG);
		this.context = context.createCopy();
		this.context.clearContent();
	}
	
	
	private boolean hasPlugin(List<Element> elements, String type) {
		for (Element element : elements) {
			if (type.equals(element.attributeValue("type"))) {
				return true;
			}
		}
		return false;
	}
	
	
	private void loadPlugins(Element context) {
		plugins = context.elements(MybatisGeneratorAnalyzer.PLUGIN_TAG);
		if (!hasPlugin(plugins, MybatisGeneratorAnalyzer.YOBATIS_PLUGIN)) {
			plugins.add(0, createYobatisPlugin());
		}
	}
	
	
	private void createTypeResolver() {
		typeResolver = factory.createElement("javaTypeResolver");
		Element property = typeResolver.addElement("property");
		property.addAttribute("name", "forceBigDecimals");
		property.addAttribute("value", "false");
	}
	
	private void createJdbcConnection(DatabaseMetadataProvider sql) {
		jdbConnection = factory.createElement("jdbcConnection");
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
	
	private Element createCriteriaPlugin() {
		Element criteriaPluginElement = factory.createElement(MybatisGeneratorAnalyzer.PLUGIN_TAG);
		criteriaPluginElement.addAttribute("type",  MybatisGeneratorAnalyzer.YOBATIS_CRITERIA_PLUGIN);
		return criteriaPluginElement;
	}
	
	private Element createYobatisPlugin() {
		Element yobatisPluginElement = factory.createElement(MybatisGeneratorAnalyzer.PLUGIN_TAG);
		yobatisPluginElement.addAttribute("type",  MybatisGeneratorAnalyzer.YOBATIS_PLUGIN);
		Element property = yobatisPluginElement.addElement("property");
		property.addAttribute("name", "enableBaseClass");
		property.addAttribute("value", "true");
		property = yobatisPluginElement.addElement("property");
		property.addAttribute("name", "enableToString");
		property.addAttribute("value", "true");
		return yobatisPluginElement;
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
		for (Element plugin: plugins) {
			context.add(plugin.createCopy());
		}
		context.add(jdbConnection.createCopy());
		context.add(typeResolver.createCopy());
		if (javaModel != null) {
			context.add(javaModel.createCopy());
		}
		if (xmlMapper != null) {
			context.add(xmlMapper.createCopy());
		}
		if (javaClient != null) {
			context.add(javaClient.createCopy());
		}
		for (Element e : tableElements) {
			context.add(e.createCopy());
		}
		return context;
	}
	
	
	private boolean hasTable(Element thatTable) {
		for (Element thisTable : tableElements) {
			String name = thisTable.attributeValue("tableName");
			String schema = thisTable.attributeValue("schema");
			if ((name != null && name.equals(thatTable.attributeValue("tableName"))) &&
				(schema != null && schema.equals(thatTable.attributeValue("schema")))) {
				return true;
			}
		}
		return false;
	}
	
	
	public void merge(MybatisGeneratorContext generatedContext) {
		Expect.asTrue(generatedContext != null && id.equals(generatedContext.id),
				"generatedContext must not be null.");
		if (jdbConnection == null) {
			jdbConnection = generatedContext.jdbConnection;
		}
		if (typeResolver == null) {
			typeResolver = generatedContext.typeResolver;
		}
		for (Element thatTable : generatedContext.tableElements) {
			if (!hasTable(thatTable)) {
				tableElements.add(thatTable);
			}
		}
	}
}
