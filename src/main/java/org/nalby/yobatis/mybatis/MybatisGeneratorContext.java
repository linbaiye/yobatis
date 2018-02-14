/*
 *    Copyright 2018 the original author or authors.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *    use this file except in compliance with the License.  You may obtain a copy
 *    of the License at
 *    
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *    License for the specific language governing permissions and limitations under
 *    the License.
 */
package org.nalby.yobatis.mybatis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.xml.AbstractXmlParser;

/**
 * Abstraction of MyBatis Generator's context.
 */
public class MybatisGeneratorContext {
	
	public final static String DEFAULT_CONTEXT_ID = "yobatis";

	public final static String TARGET_RUNTIME = "MyBatis3";

	public final static String MODEL_GENERATOR_TAG = "javaModelGenerator";

	public final static String SQLMAP_GENERATOR_TAG = "sqlMapGenerator";

	public final static String CLIENT_GENERATOR_TAG = "javaClientGenerator";

	public final static String TABLE_TAG = "table";

	public final static String PLUGIN_TAG = "plugin";

	public final static String YOBATIS_DAO_PLUGIN = "org.mybatis.generator.plugins.YobatisDaoPlugin";
	
	private DocumentFactory factory = DocumentFactory.getInstance();

	private Element context;
	
	private Element javaClient;

	private Element javaModel;

	private Element xmlMapper;
	
	private List<Element> tableElements;
	
	private Element typeResolver;
	
	private Element jdbConnection;
	
	private List<Element> plugins;
	
	private List<Element> commentedElements;
	
	private String id;

	/**
	 * Construct a MybatisGeneratorContext according to database details.
	 * @param id the context id.
	 * @param databaseMetadataProvider the database details provider.
	 */
	@SuppressWarnings("unchecked")
	public MybatisGeneratorContext(String id, DatabaseMetadataProvider databaseMetadataProvider) {
		Expect.notNull(databaseMetadataProvider, "databaseMetadataProvider must not be null.");
		Expect.notEmpty(id, "id must not be empty.");
		this.id = id;
		plugins = new ArrayList<>();
		tableElements = new ArrayList<>();
		plugins.add(createYobatisDaoPlugin());
		createJdbcConnection(databaseMetadataProvider);
		createTypeResolver();
		commentedElements = Collections.EMPTY_LIST;
	}
	
	
	/**
	 * Construct a MybatisGeneratorContext from a existent element.
	 * @param context the context element.
	 */
	public MybatisGeneratorContext(Element context) {
		Expect.notNull(context, "context must not be null.");
		this.id = context.attributeValue("id");
		Expect.notEmpty(id, "id must not be empty.");
		this.typeResolver = context.element("javaTypeResolver");
		this.jdbConnection = context.element("jdbcConnection");
		this.javaClient = context.element(CLIENT_GENERATOR_TAG);
		this.javaModel = context.element(MODEL_GENERATOR_TAG);
		this.xmlMapper = context.element(SQLMAP_GENERATOR_TAG);
		loadPlugins(context);
		this.commentedElements = AbstractXmlParser.loadCommentedElements(context);
		tableElements = context.elements(TABLE_TAG);
	}
	
	/**
	 * Test if this context has the same id to another context's id.
	 * @param thatContext 
	 * @return true if so, false else.
	 */
	public boolean idEqualsTo(MybatisGeneratorContext thatContext) {
		return thatContext != null && id.equals(thatContext.id);
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
		plugins = context.elements(PLUGIN_TAG);
		if (!hasPlugin(plugins,  YOBATIS_DAO_PLUGIN)) {
			plugins.add(0, createYobatisDaoPlugin());
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
	
	
	private Element createYobatisDaoPlugin() {
		Element yobatisPluginElement = factory.createElement(PLUGIN_TAG);
		yobatisPluginElement.addAttribute("type",   YOBATIS_DAO_PLUGIN);
		return yobatisPluginElement;
	}
	
	private Element findTable(List<Element> elements, Element table) {
		if (table != null) {
			for (Element e : elements) {
				if (!"table".equals(e.getName())) {
					continue;
				}
				String name = e.attributeValue("tableName");
				String schema = e.attributeValue("schema");
				if ((name != null && name.equals(table.attributeValue("tableName")))
						&& (schema != null && schema.equals(table.attributeValue("schema")))) {
					return e;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Test if this table element is contained in this context by comparing
	 * the tableName and schema attributes.
	 * @param table the table element to test.
	 * @return true if so, false otherwise.
	 */
	private boolean hasTable(Element thatTable) {
		return findTable(tableElements, thatTable) != null;
	}
	
						
	/**
	 * Test if this context contains any table elements.
	 * @return true if so, false otherwise.
	 */
	public boolean hasTable() {
		return !tableElements.isEmpty();
	}
	
	
	/**
	 * Remove tables appearing in thatContext from this context.
	 */
	public void removeExistentTables(MybatisGeneratorContext thatContext) {
		Expect.notNull(thatContext, "thatContext must not be null.");
		for (Iterator<Element> iterator = tableElements.iterator(); iterator.hasNext(); ) {
			Element thisTable = iterator.next();
			String thisTableName = thisTable.attributeValue("tableName");
			String thisSchema = thisTable.attributeValue("schema");
			if (thisTableName == null || thisSchema == null) {
				continue;
			}
			if (findTable(thatContext.tableElements, thisTable) != null ||
				findTable(thatContext.commentedElements, thisTable) != null) {
				iterator.remove();
			}
		}
	}

	
	/**
	 * Create the javaModelGenerator element according to the model folder,
	 * if the model folder is null, both of the targetPackage and targetProject
	 * will be empty.
	 * 
	 * @param folder the model folder.
	 */
	public void createJavaModelGenerator(Folder folder) {
		javaModel = factory.createElement(MODEL_GENERATOR_TAG);
		javaModel.addAttribute("targetPackage", packageNameOfFolder(folder));
		javaModel.addAttribute("targetProject", sourceCodePath(folder));
	}

	/**
	 * Create the sqlMapGenerator element according to the resource folder,
	 * if the model folder is null, the targetProject will be empty.
	 * 
	 * @param folder the resource folder.
	 */
	public void createSqlMapGenerator(Folder folder) {
		xmlMapper = factory.createElement(SQLMAP_GENERATOR_TAG);
		xmlMapper.addAttribute("targetPackage", "mybatis-mappers");
		xmlMapper.addAttribute("targetProject", folder == null? "" : folder.path());
	}
	
	/**
	 * Create the sqlMapGenerator element according to the dao folder,
	 * if the dao folder is null, both of the targetPackage and targetProject
	 * will be empty.
	 * 
	 * @param folder the dao/mapper folder.
	 */
	public void createJavaClientGenerator(Folder folder) {
		javaClient = factory.createElement(CLIENT_GENERATOR_TAG);
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
	
	
	/**
	 * Get the context element.
	 * @return a dom4j element that represents this context.
	 */
	public Element getContext() {
		if (context != null) {
			return context;
		}
		context = factory.createElement("context");
		context.addAttribute("id", id);
		context.addAttribute("targetRuntime",  TARGET_RUNTIME);
		for (Element plugin: plugins) {
			context.add(plugin.createCopy());
		}
		if (jdbConnection != null) {
			context.add(jdbConnection.createCopy());
		}
		if (typeResolver != null) {
			context.add(typeResolver.createCopy());
		}
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
		for (Element e : commentedElements) {
			if ("table".equals(e.getName())) {
				context.add(AbstractXmlParser.commentElement(e));
			}
		}
		return context;
	}
	
	/**
	 * Merge jdbcConnection element and table elements.
	 * @param generatedContext another context to merge.
	 */
	public void merge(MybatisGeneratorContext generatedContext) {
		Expect.asTrue(generatedContext != null && id.equals(generatedContext.id),
				"generatedContext must not be null.");
		if (jdbConnection == null) {
			jdbConnection = generatedContext.jdbConnection;
		}
		for (Element thatTable : generatedContext.tableElements) {
			if (!hasTable(thatTable)) {
				tableElements.add(thatTable.createCopy());
			}
		}
	}
}
