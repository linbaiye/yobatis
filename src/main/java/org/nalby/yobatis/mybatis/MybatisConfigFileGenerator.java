package org.nalby.yobatis.mybatis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.xml.MybatisXmlParser;

/**
 * Used to generate mybaits generator's config file according to
 * current project structure.
 * @author Kyle Lin
 */
public class MybatisConfigFileGenerator implements MybatisConfigReader {
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
	
	private Element pluginElement;

	private Element pagingAndLockElement;

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
		appendRenamePlugin(context);
		appendPagingAndLock(context);
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
	
	public Element getPluginElement() {
		return pluginElement;
	}
	
	public Element getPagingAndLockElement() {
		return pagingAndLockElement;
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
		} else if (paths.size() > 1) {
			errorCode = ErrorCode.MULTIPLE_MODEL_PATHS;
		}
		for (String path: paths) {
			String packageName = getPackageName(path);
			Element javaModelGenerator = context.addElement("javaModelGenerator");
			javaModelGenerator.addAttribute("targetPackage", packageName == null? "": packageName);
			javaModelGenerator.addAttribute("targetProject", eliminatePackagePath(path));
			javaModelGenerators.add(javaModelGenerator);
		}
	}

	private void appendPagingAndLock(Element context) {
		pagingAndLockElement = context.addElement("plugin");
		pagingAndLockElement.addAttribute("type",  "org.mybatis.generator.plugins.PagingAndLockPlugin");
	}
	
	private void appendRenamePlugin(Element context) {
		pluginElement = context.addElement("plugin");
		pluginElement.addAttribute("type",  "org.mybatis.generator.plugins.RenameExampleClassPlugin");
		Element property = pluginElement.addElement("property");
		property.addAttribute("name", "searchString");
		property.addAttribute("value", "Example$");
		property = pluginElement.addElement("property");
		property.addAttribute("name", "replaceString");
		property.addAttribute("value", "Criteria");
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
	
	private final static Pattern PATTERN = Pattern.compile("^.+" + Project.MAVEN_SOURCE_CODE_PATH + "/(.+)$");
	
	private  String getPackageName(String path) {
		if (path == null || !path.contains(Project.MAVEN_SOURCE_CODE_PATH)) {
			return null;
		}
		Matcher matcher = PATTERN.matcher(path);
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
		Matcher matcher = PATTERN.matcher(fullpath);
		String ret = null;
		if (matcher.find()) {
			ret = matcher.group(1);
		}
		if (ret == null) {
			return fullpath;
		}
		return fullpath.replace(ret, "");
	}
	
	private void appendJavaClientGenerator(Element context) {
		List<String> paths = project.getSyspathsOfDao();
		if (paths.isEmpty()) {
			errorCode = ErrorCode.NO_DAO_PATH;
		} else if (paths.size() > 1) {
			errorCode = ErrorCode.MULTIPLE_MODEL_PATHS;
		}
		for (String path: paths) {
			String packageName = getPackageName(path);
			Element generator = context.addElement("javaClientGenerator");
			generator.addAttribute("type", "XMLMAPPER");
			generator.addAttribute("targetPackage", packageName == null ? "" : packageName);
			generator.addAttribute("targetProject", eliminatePackagePath(path));
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
	
	
	private Element findActiveElement(Set<Element> generators, String name) {
		if (generators == null || generators.isEmpty()) {
			throw new InvalidMybatisGeneratorConfigException(
					String.format("There is no %s configured, please set the element and re-run.", name));
		}
		if (generators.size() > 1) {
			throw new InvalidMybatisGeneratorConfigException(
					String.format("More than one %s configured, please remove unintentional ones and re-run.", name));
		}
		Iterator<Element> iterator =  generators.iterator();
		while (iterator.hasNext()) {
			Element element = iterator.next();
			return element;
		}
		throw new InvalidMybatisGeneratorConfigException("Should not happen.");
	}

	private String glueTargetPackageToTargetProject(Set<Element> generators, String name) {
		Element element = findActiveElement(generators, name);
		String packageName = element.attributeValue("targetPackage");
		String targetProject = element.attributeValue("targetProject");
		return targetProject + "/" + packageName.replace(".", "/");
	}

	@Override
	public String getDaoDirPath() {
		return glueTargetPackageToTargetProject(javaClientGenerators, "javaClientGenerator");
	}

	@Override
	public String getDomainDirPath() {
		return glueTargetPackageToTargetProject(javaModelGenerators, "javaModelGenerator");
	}

	@Override
	public String getCriteriaDirPath() {
		String daoPath =  glueTargetPackageToTargetProject(javaModelGenerators, "javaModelGenerator");
		return daoPath + "/criteria";
	}

	@Override
	public String getConfigeFilename() {
		return CONFIG_FILENAME;
	}

	@Override
	public String getPackageNameOfDomains() {
		Element element = findActiveElement(javaModelGenerators, "javaModelGenerator");
		return element.attributeValue("targetPackage");
	}

	@Override
	public String getMapperDirPath() {
		return glueTargetPackageToTargetProject(sqlMapGenerators, "sqlMapGenerator");
	}

	@Override
	public String getPackageNameOfJavaMappers() {
		Element element = findActiveElement(javaClientGenerators, "javaClientGenerator");
		return element.attributeValue("targetPackage");
	}
	
}
