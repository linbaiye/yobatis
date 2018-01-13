package org.nalby.yobatis.mybatis;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.PomTree;
import org.nalby.yobatis.xml.AbstractXmlParser;

/**
 * Generate MyBaits Generator's configuration file according to current project structure.
 * 
 * @author Kyle Lin
 */
public class MybatisGeneratorXmlCreator implements MybatisGeneratorAnalyzer {

	private Document document;

	private DatabaseMetadataProvider sql;

	private PomTree pomTree;
	
	private DocumentFactory factory = DocumentFactory.getInstance();

	private Element root;

	private Element classPathEntry;
	
	private Element context;
	
	private Element jdbConnection;
	
	private Element typeResolver;
	
	private Element pluginElement;

	private Element criteriaPluginElement;

	private Set<Element> javaModelGenerators = new HashSet<Element>();

	private Set<Element> sqlMapGenerators = new HashSet<Element>();
	
	private Set<Element> javaClientGenerators = new HashSet<Element>();

	private Set<Element> tableElemnts = new HashSet<Element>();
	
	private Logger logger = LogFactory.getLogger(MybatisGeneratorXmlCreator.class);
	
	public MybatisGeneratorXmlCreator(PomTree pomTree, DatabaseMetadataProvider sql) {
		this.sql = sql;
		this.pomTree = pomTree;
		createDocument();
		root = factory.createElement(ROOT_TAG);
		document.setRootElement(root);
		appendClassPathEntry(root);
		context = appendContext(root);
		appendYobatisPlugin(context);
		appendCriteriaPlugin(context);
		appendJdbcConnection(context);
		appendTypeResolver(context);
		appendJavaModelGenerator(context);
		appendSqlMapGenerator(context);
		appendJavaClientGenerator(context);
		appendTables(context);
		logger.info("Generated MyBatis Generator's configuration file.");
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
		return tableElemnts;
	}
	
	public Element getContext() {
		return context;
	}
	
	public Element getPluginElement() {
		return pluginElement;
	}
	
	public Element getCriteriaPluginElement() {
		return criteriaPluginElement;
	}
	
	private void appendClassPathEntry(Element root) {
		classPathEntry = root.addElement(MybatisGeneratorXmlReader.CLASS_PATH_ENTRY_TAG);
		classPathEntry.addAttribute("location", sql.getConnectorJarPath());
	}
	
	private void appendTypeResolver(Element root) {
		typeResolver = root.addElement("javaTypeResolver");
		Element property = typeResolver.addElement("property");
		property.addAttribute("name", "forceBigDecimals");
		property.addAttribute("value", "false");
	}
	
	private void appendTables(Element context)  {
		for (Table table: sql.getTables()) {
			Element element = context.addElement("table");
			element.addAttribute("tableName", table.getName());
			element.addAttribute("schema", sql.getSchema());
			element.addAttribute("modelType", "flat");
			String autoIncKey = table.getAutoIncPK();
			if (autoIncKey != null) {
				Element pk = element.addElement("generatedKey");
				pk.addAttribute("column", autoIncKey);
				pk.addAttribute("sqlStatement", "mysql");
				pk.addAttribute("identity", "true");
			}
			tableElemnts.add(element);
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
		List<Folder> folders = pomTree.lookupModelFolders();
		for (Folder folder: folders) {
			String path = folder.path();
			String packageName = getPackageName(path);
			Element javaModelGenerator = context.addElement(MODEL_GENERATOR_TAG);
			javaModelGenerator.addAttribute("targetPackage", packageName == null? "": packageName);
			javaModelGenerator.addAttribute("targetProject", eliminatePackagePath(path));
			javaModelGenerators.add(javaModelGenerator);
		}
	}
	
	private void appendJavaClientGenerator(Element context) {
		List<Folder> folders = pomTree.lookupDaoFolders();
		for (Folder folder: folders) {
			String path = folder.path();
			String packageName = getPackageName(path);
			Element generator = context.addElement(CLIENT_GENERATOR_TAG);
			generator.addAttribute("type", "XMLMAPPER");
			generator.addAttribute("targetPackage", packageName == null ? "" : packageName);
			generator.addAttribute("targetProject", eliminatePackagePath(path));
			javaClientGenerators.add(generator);
		}
	}

	private void appendCriteriaPlugin(Element context) {
		criteriaPluginElement = context.addElement("plugin");
		criteriaPluginElement.addAttribute("type",  YOBATIS_CRITERIA_PLUGIN);
	}
	
	private void appendYobatisPlugin(Element context) {
		pluginElement = context.addElement("plugin");
		pluginElement.addAttribute("type",  YOBATIS_PLUGIN);
		Element property = pluginElement.addElement("property");
		property.addAttribute("name", "enableBaseClass");
		property.addAttribute("value", "true");
	}
	
	private Element appendContext(Element root) {
		context = root.addElement("context");
		context.addAttribute("id", MybatisGeneratorAnalyzer.DEFAULT_CONTEXT_ID);
		context.addAttribute("targetRuntime", MybatisGeneratorAnalyzer.TARGET_RUNTIME);
		return context;
	}
	
	private void appendSqlMapGenerator(Element context) {
		List<Folder> resourceFolders = pomTree.lookupResourceFolders();
		for (Folder folder: resourceFolders) {
			String path = folder.path();
			Element generator = context.addElement(SQLMAP_GENERATOR_TAG);
			generator.addAttribute("targetPackage", "mybatis-mappers");
			generator.addAttribute("targetProject", path);
			sqlMapGenerators.add(generator);
		}
	}

	private final static String MAVEN_SOURCE_CODE_PATH = "src/main/java";

	private final static Pattern SOURCE_CODE_PATTERN = Pattern.compile("^.+" + MAVEN_SOURCE_CODE_PATH + "/(.+)$");
	private String getPackageName(String path) {
		if (path == null || !path.contains(MAVEN_SOURCE_CODE_PATH)) {
			return null;
		}
		Matcher matcher = SOURCE_CODE_PATTERN.matcher(path);
		String ret = null;
		if (matcher.find()) {
			ret = matcher.group(1);
		}
		if (ret != null) {
			ret = ret.replaceAll("/", ".");
		}
		return ret;
	}
	
	private String eliminatePackagePath(String fullpath) {
		Matcher matcher = SOURCE_CODE_PATTERN.matcher(fullpath);
		String ret = null;
		if (matcher.find()) {
			ret = matcher.group(1);
		}
		if (ret == null) {
			return fullpath;
		}
		return fullpath.replace("/" + ret, "");
	}
	
	
	
	private void createDocument() {
		document = factory.createDocument();
		DocumentType type = factory.createDocType(ROOT_TAG, "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN", "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd");
		document.setDocType(type);
	}
	
	public String asXmlText() {
		try {
			return AbstractXmlParser.toXmlString(document);
		} catch (IOException e) {
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
	
	private Element findActiveElement(Set<Element> generators, String name) {
		if (generators == null || generators.isEmpty()) {
			throw new InvalidMybatisGeneratorConfigException(
					String.format("There is no %s configured, please set the element and re-run.", name));
		} else if (generators.size() > 1) {
			throw new InvalidMybatisGeneratorConfigException(
					String.format("More than one %s configured, please remove unintentional ones and re-run.", name));
		} else {
			return generators.iterator().next();
		}
	}

	private String buildPathFromGenerator(Set<Element> generators, String name) {
		Element element = findActiveElement(generators, name);
		String packageName = element.attributeValue("targetPackage");
		String targetProject = element.attributeValue("targetProject");
		return targetProject + "/" + packageName.replace(".", "/");
	}

	@Override
	public String getDaoDirPath() {
		return buildPathFromGenerator(javaClientGenerators, CLIENT_GENERATOR_TAG);
	}

	@Override
	public String getModelDirPath() {
		return buildPathFromGenerator(javaModelGenerators, MODEL_GENERATOR_TAG);
	}

	@Override
	public String getCriteriaDirPath() {
		String daoPath =  buildPathFromGenerator(javaModelGenerators, MODEL_GENERATOR_TAG);
		return daoPath + "/criteria";
	}

	@Override
	public String getModelPackageName() {
		Element element = findActiveElement(javaModelGenerators, MODEL_GENERATOR_TAG);
		return element.attributeValue("targetPackage");
	}

	@Override
	public String getXmlMapperDirPath() {
		return buildPathFromGenerator(sqlMapGenerators, SQLMAP_GENERATOR_TAG);
	}

	@Override
	public String getDaoPackageName() {
		Element element = findActiveElement(javaClientGenerators, CLIENT_GENERATOR_TAG);
		return element.attributeValue("targetPackage");
	}
	
}
