package org.nalby.yobatis.mybatis;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;

/**
 * 
 * 
 *
 */
public class MybatisGeneratorContext {
	
	public final static String DEFAULT_CONTEXT_ID = "yobatis";

	public final static String TARGET_RUNTIME = "MyBatis3";

	public final static String MODEL_GENERATOR_TAG = "javaModelGenerator";

	public final static String SQLMAP_GENERATOR_TAG = "sqlMapGenerator";

	public final static String CLIENT_GENERATOR_TAG = "javaClientGenerator";

	public final static String TABLE_TAG = "table";

	public final static String PLUGIN_TAG = "plugin";

	public final static String YOBATIS_PLUGIN = "org.mybatis.generator.plugins.YobatisPlugin";
	
	public final static String YOBATIS_CRITERIA_PLUGIN = "org.mybatis.generator.plugins.YobatisCriteriaPlugin";
	
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
		plugins.add(createYobatisPlugin());
		plugins.add(createCriteriaPlugin());
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
		loadCommentedElements(context);
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
		if (!hasPlugin(plugins,  YOBATIS_PLUGIN)) {
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
		Element criteriaPluginElement = factory.createElement(PLUGIN_TAG);
		criteriaPluginElement.addAttribute("type",   YOBATIS_CRITERIA_PLUGIN);
		return criteriaPluginElement;
	}
	
	private Element createYobatisPlugin() {
		Element yobatisPluginElement = factory.createElement(PLUGIN_TAG);
		yobatisPluginElement.addAttribute("type",   YOBATIS_PLUGIN);
		Element property = yobatisPluginElement.addElement("property");
		property.addAttribute("name", "enableBaseClass");
		property.addAttribute("value", "true");
		property = yobatisPluginElement.addElement("property");
		property.addAttribute("name", "enableToString");
		property.addAttribute("value", "true");
		return yobatisPluginElement;
	}
	
	private Document buildDoc(String text) {
		SAXReader saxReader = new SAXReader();
		saxReader.setValidation(false);
		try  {
			return saxReader.read(new ByteArrayInputStream(("<rootDoc>" + text + "</rootDoc>").getBytes()));
		} catch (Exception e) {
			return null;
		}
	}
	
	private void convertToElements(String text) {
		Document doc = buildDoc(text);
		if (doc != null) {
			commentedElements = doc.getRootElement().elements();
		}
		if (commentedElements == null) {
			commentedElements = new ArrayList<>(0);
		}
	}
	
	private boolean isCommentedElement(String text) {
		if (buildDoc(text) != null) {
			return true;
		}
		return false;
	}
	
	private void loadCommentedElements(Element parent)  {
		String text = null;
		for (Iterator<Node> iterator = parent.nodeIterator(); iterator.hasNext(); ) {
			Node node = iterator.next();
			if (node.getNodeType() != Node.COMMENT_NODE) {
				continue;
			}
			Comment comment = (Comment) node;
			String tmp = comment.asXML().replaceAll("\\s+", " ");
			tmp = tmp.replaceAll("<!--", "<");
			tmp = tmp.replaceAll("-->", ">");
			if (isCommentedElement(tmp)) {
				text = text == null ? tmp : text + tmp;
			}
		}
		convertToElements(text);
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
	 * Test if this table element is commented by comparing
	 * the tableName and schema attributes.
	 * @param table the table element to test.
	 * @return true if so, false otherwise.
	 */
	public boolean isTableCommented(Element table) {
		return findTable(commentedElements, table) != null;
	}
	
	/**
	 * Test if this table element is contained in this context by comparing
	 * the tableName and schema attributes.
	 * @param table the table element to test.
	 * @return true if so, false otherwise.
	 */
	public boolean hasTable(Element thatTable) {
		return findTable(tableElements, thatTable) != null;
	}

	
	public void appendJavaModelGenerator(Folder folder) {
		javaModel = factory.createElement(MODEL_GENERATOR_TAG);
		javaModel.addAttribute("targetPackage", packageNameOfFolder(folder));
		javaModel.addAttribute("targetProject", sourceCodePath(folder));
	}

	public void appendSqlMapGenerator(Folder folder) {
		xmlMapper = factory.createElement(SQLMAP_GENERATOR_TAG);
		xmlMapper.addAttribute("targetPackage", "mybatis-mappers");
		xmlMapper.addAttribute("targetProject", folder == null? "" : folder.path());
	}
	
	public void appendJavaClientGenerator(Folder folder) {
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
		return context;
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
				tableElements.add(thatTable.createCopy());
			}
		}
	}
}
