package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Comment;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;
import org.nalby.yobatis.mybatis.MybatisConfigReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MybatisXmlParser extends BasicXmlParser implements MybatisConfigReader {
	private static final String DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<!--\n" + 
			"\n" + 
			"       Copyright 2006-2016 the original author or authors.\n" + 
			"\n" + 
			"       Licensed under the Apache License, Version 2.0 (the \"License\");\n" + 
			"       you may not use this file except in compliance with the License.\n" + 
			"       You may obtain a copy of the License at\n" + 
			"\n" + 
			"          http://www.apache.org/licenses/LICENSE-2.0\n" + 
			"\n" + 
			"       Unless required by applicable law or agreed to in writing, software\n" + 
			"       distributed under the License is distributed on an \"AS IS\" BASIS,\n" + 
			"       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" + 
			"       See the License for the specific language governing permissions and\n" + 
			"       limitations under the License.\n" + 
			"\n" + 
			"-->\n" + 
			"<!--\n" + 
			"  This DTD defines the structure of the MyBatis generator configuration file.\n" + 
			"  Configuration files should declare the DOCTYPE as follows:\n" + 
			"  \n" + 
			"  <!DOCTYPE generatorConfiguration PUBLIC\n" + 
			"    \"-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN\"\n" + 
			"    \"http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd\">\n" + 
			"  \n" + 
			"  Please see the documentation included with MyBatis generator for details on each option\n" + 
			"  in the DTD.  You may also view documentation on-line here:\n" + 
			"  \n" + 
			"  http://www.mybatis.org/generator/\n" + 
			"  \n" + 
			"-->\n" + 
			"\n" + 
			"<!--\n" + 
			"  The generatorConfiguration element is the root element for configurations.\n" + 
			"-->\n" + 
			"<!ELEMENT generatorConfiguration (properties?, classPathEntry*, context+)>\n" + 
			"                        \n" + 
			"<!--\n" + 
			"  The properties element is used to define a standard Java properties file\n" + 
			"  that contains placeholders for use in the remainder of the configuration\n" + 
			"  file.\n" + 
			"-->\n" + 
			"<!ELEMENT properties EMPTY>\n" + 
			"<!ATTLIST properties\n" + 
			"  resource CDATA #IMPLIED\n" + 
			"  url CDATA #IMPLIED>\n" + 
			"  \n" + 
			"<!--\n" + 
			"  The context element is used to describe a context for generating files, and the source\n" + 
			"  tables.\n" + 
			"-->\n" + 
			"<!ELEMENT context (property*, plugin*, commentGenerator?, (connectionFactory | jdbcConnection), javaTypeResolver?,\n" + 
			"                         javaModelGenerator, sqlMapGenerator?, javaClientGenerator?, table+)>\n" + 
			"<!ATTLIST context id ID #REQUIRED\n" + 
			"  defaultModelType CDATA #IMPLIED\n" + 
			"  targetRuntime CDATA #IMPLIED\n" + 
			"  introspectedColumnImpl CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The connectionFactory element is used to describe the connection factory used\n" + 
			"  for connecting to the database for introspection.  Either connectionFacoty\n" + 
			"  or jdbcConnection must be specified, but not both.\n" + 
			"-->\n" + 
			"<!ELEMENT connectionFactory (property*)>\n" + 
			"<!ATTLIST connectionFactory\n" + 
			"  type CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The jdbcConnection element is used to describe the JDBC connection that the generator\n" + 
			"  will use to introspect the database.\n" + 
			"-->\n" + 
			"<!ELEMENT jdbcConnection (property*)>\n" + 
			"<!ATTLIST jdbcConnection \n" + 
			"  driverClass CDATA #REQUIRED\n" + 
			"  connectionURL CDATA #REQUIRED\n" + 
			"  userId CDATA #IMPLIED\n" + 
			"  password CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The classPathEntry element is used to add the JDBC driver to the run-time classpath.\n" + 
			"  Repeat this element as often as needed to add elements to the classpath.\n" + 
			"-->\n" + 
			"<!ELEMENT classPathEntry EMPTY>\n" + 
			"<!ATTLIST classPathEntry\n" + 
			"  location CDATA #REQUIRED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The property element is used to add custom properties to many of the generator's\n" + 
			"  configuration elements.  See each element for example properties.\n" + 
			"  Repeat this element as often as needed to add as many properties as necessary\n" + 
			"  to the configuration element.\n" + 
			"-->\n" + 
			"<!ELEMENT property EMPTY>\n" + 
			"<!ATTLIST property\n" + 
			"  name CDATA #REQUIRED\n" + 
			"  value CDATA #REQUIRED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The plugin element is used to define a plugin.\n" + 
			"-->\n" + 
			"<!ELEMENT plugin (property*)>\n" + 
			"<!ATTLIST plugin\n" + 
			"  type CDATA #REQUIRED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The javaModelGenerator element is used to define properties of the Java Model Generator.\n" + 
			"  The Java Model Generator builds primary key classes, record classes, and Query by Example \n" + 
			"  indicator classes.\n" + 
			"-->\n" + 
			"<!ELEMENT javaModelGenerator (property*)>\n" + 
			"<!ATTLIST javaModelGenerator\n" + 
			"  targetPackage CDATA #REQUIRED\n" + 
			"  targetProject CDATA #REQUIRED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The javaTypeResolver element is used to define properties of the Java Type Resolver.\n" + 
			"  The Java Type Resolver is used to calculate Java types from database column information.\n" + 
			"  The default Java Type Resolver attempts to make JDBC DECIMAL and NUMERIC types easier\n" + 
			"  to use by substituting Integral types if possible (Long, Integer, Short, etc.)\n" + 
			"-->\n" + 
			"<!ELEMENT javaTypeResolver (property*)>\n" + 
			"<!ATTLIST javaTypeResolver\n" + 
			"  type CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The sqlMapGenerator element is used to define properties of the SQL Map Generator.\n" + 
			"  The SQL Map Generator builds an XML file for each table that conforms to iBATIS'\n" + 
			"  SqlMap DTD.\n" + 
			"-->\n" + 
			"<!ELEMENT sqlMapGenerator (property*)>\n" + 
			"<!ATTLIST sqlMapGenerator\n" + 
			"  targetPackage CDATA #REQUIRED\n" + 
			"  targetProject CDATA #REQUIRED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The javaClientGenerator element is used to define properties of the Java client Generator.\n" + 
			"  The Java Client Generator builds Java interface and implementation classes\n" + 
			"  (as required) for each table.\n" + 
			"  If this element is missing, then the generator will not build Java Client classes.\n" + 
			"-->\n" + 
			"<!ELEMENT javaClientGenerator (property*)>\n" + 
			"<!ATTLIST javaClientGenerator\n" + 
			"  type CDATA #REQUIRED\n" + 
			"  targetPackage CDATA #REQUIRED\n" + 
			"  targetProject CDATA #REQUIRED\n" + 
			"  implementationPackage CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The table element is used to specify a database table that will be the source information\n" + 
			"  for a set of generated objects.\n" + 
			"-->\n" + 
			"<!ELEMENT table (property*, generatedKey?, columnRenamingRule?, (columnOverride | ignoreColumn | ignoreColumnsByRegex)*) >\n" + 
			"<!ATTLIST table\n" + 
			"  catalog CDATA #IMPLIED\n" + 
			"  schema CDATA #IMPLIED\n" + 
			"  tableName CDATA #REQUIRED\n" + 
			"  alias CDATA #IMPLIED\n" + 
			"  domainObjectName CDATA #IMPLIED\n" + 
			"  mapperName CDATA #IMPLIED\n" + 
			"  sqlProviderName CDATA #IMPLIED\n" + 
			"  enableInsert CDATA #IMPLIED\n" + 
			"  enableSelectByPrimaryKey CDATA #IMPLIED\n" + 
			"  enableSelectByExample CDATA #IMPLIED\n" + 
			"  enableUpdateByPrimaryKey CDATA #IMPLIED\n" + 
			"  enableDeleteByPrimaryKey CDATA #IMPLIED\n" + 
			"  enableDeleteByExample CDATA #IMPLIED\n" + 
			"  enableCountByExample CDATA #IMPLIED\n" + 
			"  enableUpdateByExample CDATA #IMPLIED\n" + 
			"  selectByPrimaryKeyQueryId CDATA #IMPLIED\n" + 
			"  selectByExampleQueryId CDATA #IMPLIED\n" + 
			"  modelType CDATA #IMPLIED\n" + 
			"  escapeWildcards CDATA #IMPLIED\n" + 
			"  delimitIdentifiers CDATA #IMPLIED\n" + 
			"  delimitAllColumns CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The columnOverride element is used to change certain attributes of the column\n" + 
			"  from their default values.\n" + 
			"-->\n" + 
			"<!ELEMENT columnOverride (property*)>\n" + 
			"<!ATTLIST columnOverride\n" + 
			"  column CDATA #REQUIRED\n" + 
			"  property CDATA #IMPLIED\n" + 
			"  javaType CDATA #IMPLIED\n" + 
			"  jdbcType CDATA #IMPLIED\n" + 
			"  typeHandler CDATA #IMPLIED\n" + 
			"  isGeneratedAlways CDATA #IMPLIED\n" + 
			"  delimitedColumnName CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The ignoreColumn element is used to identify a column that should be ignored.\n" + 
			"  No generated SQL will refer to the column, and no property will be generated\n" + 
			"  for the column in the model objects.\n" + 
			"-->\n" + 
			"<!ELEMENT ignoreColumn EMPTY>\n" + 
			"<!ATTLIST ignoreColumn\n" + 
			"  column CDATA #REQUIRED\n" + 
			"  delimitedColumnName CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The ignoreColumnsByRegex element is used to identify a column pattern that should be ignored.\n" + 
			"  No generated SQL will refer to the column, and no property will be generated\n" + 
			"  for the column in the model objects.\n" + 
			"-->\n" + 
			"<!ELEMENT ignoreColumnsByRegex (except*)>\n" + 
			"<!ATTLIST ignoreColumnsByRegex\n" + 
			"  pattern CDATA #REQUIRED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The except element is used to identify an exception to the ignoreColumnsByRegex rule.\n" + 
			"  If a column matches the regex rule, but also matches the exception, then the\n" + 
			"  column will be included in the generated objects.\n" + 
			"-->\n" + 
			"<!ELEMENT except EMPTY>\n" + 
			"<!ATTLIST except\n" + 
			"  column CDATA #REQUIRED\n" + 
			"  delimitedColumnName CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The generatedKey element is used to identify a column in the table whose value\n" + 
			"  is calculated - either from a sequence (or some other query), or as an identity column.\n" + 
			"-->\n" + 
			"<!ELEMENT generatedKey EMPTY>\n" + 
			"<!ATTLIST generatedKey\n" + 
			"  column CDATA #REQUIRED\n" + 
			"  sqlStatement CDATA #REQUIRED\n" + 
			"  identity CDATA #IMPLIED\n" + 
			"  type CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The columnRenamingRule element is used to specify a rule for renaming\n" + 
			"  columns before the corresponding property name is calculated\n" + 
			"-->\n" + 
			"<!ELEMENT columnRenamingRule EMPTY>\n" + 
			"<!ATTLIST columnRenamingRule\n" + 
			"  searchString CDATA #REQUIRED\n" + 
			"  replaceString CDATA #IMPLIED>\n" + 
			"\n" + 
			"<!--\n" + 
			"  The commentGenerator element is used to define properties of the Comment Generator.\n" + 
			"  The Comment Generator adds comments to generated elements.\n" + 
			"-->\n" + 
			"<!ELEMENT commentGenerator (property*)>\n" + 
			"<!ATTLIST commentGenerator\n" + 
			"  type CDATA #IMPLIED>\n" + 
			" ";

	private Element root;
	
	private Element classPathEntry;
	
	private Element context;
	
	private Element jdbcConnection;
	
	private Element javaTypeResolver;
	
	private Element pluginElement;

	private Element pagingAndLockElement;
	
	private DocumentFactory documentFactory  = DocumentFactory.getInstance();
	
	private Set<Node> javaModelGenerators = new HashSet<Node>();

	private Set<Node> sqlMapGenerators = new HashSet<Node>();

	private Set<Node> javaClientGenerators = new HashSet<Node>();

	private Set<Node> tables = new HashSet<Node>();
	
	public final static String CLASS_PATH_ENTRY_TAG = "classPathEntry";
	public final static String MODEL_GENERATOR_TAG = "javaModelGenerator";
	public final static String SQLMAP_GENERATOR_TAG = "sqlMapGenerator";
	public final static String CLIENT_GENERATOR_TAG = "javaClientGenerator";
	public final static String TABLE_TAG = "table";
	public final static String ROOT_TAG = "generatorConfiguration";
	public final static String CONTEXT_ID = "yobatis";
	public final static String TARGET_RUNTIME = "MyBatis3";

	public MybatisXmlParser(InputStream inputStream) throws DocumentException, IOException {
		super(inputStream, ROOT_TAG);
		root = document.getRootElement();
		loadClasspathEntry();
		loadContext();
		loadJdbcConnection();
		loadJavaTypeResolver();
		loadNodes(MODEL_GENERATOR_TAG,  javaModelGenerators);
		loadNodes(SQLMAP_GENERATOR_TAG,  sqlMapGenerators);
		loadNodes(CLIENT_GENERATOR_TAG,  javaClientGenerators);
		loadTables();
		loadRenamePlugin();
		loadPagingAndLockPlugin();
		document.remove(root);
		root = documentFactory.createElement(ROOT_TAG);
		document.setRootElement(root);
	}
	
	private void loadClasspathEntry() {
		classPathEntry = root.element(CLASS_PATH_ENTRY_TAG);	
		if (classPathEntry != null) {
			classPathEntry.detach();
		}
	}
	
	private void loadTables() {
		loadNodes(TABLE_TAG,  tables);
		for (Iterator<Node> iterator = context.nodeIterator(); iterator.hasNext(); ) {
			Node node = iterator.next();
			if (node.getNodeType() != Node.COMMENT_NODE) {
				continue;
			}
			iterator.remove();
			Comment comment = (Comment) node;
			String tmp = comment.getText().replace("<!--", "");
			if (tmp.trim().startsWith("table")) {
				tables.add(node.detach());
			}
		}
	}
	
	private void loadRenamePlugin() {
		List<Element> elemtns = context.elements("plugin");
		for (Element element : elemtns) {
			if ("org.mybatis.generator.plugins.RenameExampleClassPlugin".equals(element.attributeValue("type"))) {
				pluginElement = context.element("plugin");
				pluginElement.detach();
				return;
			}
		}
	}
	
	
	private void loadPagingAndLockPlugin() {
		List<Element> elemtns = context.elements("plugin");
		for (Element element : elemtns) {
			if ("org.mybatis.generator.plugins.PagingAndLockPlugin".equals(element.attributeValue("type"))) {
				pagingAndLockElement = context.element("plugin");
				pagingAndLockElement.detach();
				return;
			}
		}
	}
	
	private void loadContext() {
		context = root.element("context");
		if (context != null) {
			context.detach();
		}
	}
	
	private void loadJdbcConnection() {
		jdbcConnection = context.element("jdbcConnection");
		if (jdbcConnection != null) {
			jdbcConnection.detach();
		}
	}
	
	private void loadJavaTypeResolver() {
		javaTypeResolver = context.element("javaTypeResolver");
		if (javaTypeResolver != null) {
			javaTypeResolver.detach();
		}	
	}

	private void loadNodes(String name, Set<Node> set) {
		List<Element> list = context.elements(name);
		for (Element element: list) {
			set.add(element.detach());
		}
	}
	
	private boolean hasGenerator(Element element, Set<Node> set) {
		for (Node node : set) {
			if (!(node instanceof Element)) {
				continue;
			}
			Element e = (Element) node;
			if (e.attributeValue("targetPackage").equals(element.attributeValue("targetPackage")) &&
				e.attributeValue("targetProject").equals(element.attributeValue("targetProject"))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasTable(Element table) {
		for (Node node: tables) {
			if (!(node instanceof Element)) {
				continue;
			}
			Element e = (Element) node;
			if (e.attributeValue("tableName").equals(table.attributeValue("tableName"))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isTableCommented(Element table) {
		for (Node node: tables) {
			if (!(node instanceof Comment)) {
				continue;
			}
			Comment comment = (Comment)node;
			String text = comment.getText().replaceAll("\\s", "");
			if (text.contains("tableName=\"" + table.attributeValue("tableName") + "\"")
				&& text.contains("schema=\"" + table.attributeValue("schema") + "\"")) {
				return true;
			}
		}
		return false;
	}
	
	private Comment commentElement(Element e) {
		String str = e.asXML();
		str = str.replaceFirst("^<", "");
		str = str.replaceFirst("/>$", "");
		return documentFactory.createComment(str);
	}

	
	private void appendGenerators(Set<Element> generatedElements, Set<Node> existentElements) {
		boolean needComment = existentElements.isEmpty() ? false : true;
		for (Element e: generatedElements) {
			if (!hasGenerator(e, existentElements)) {
				//Add commented element if the existent generators don't have this one.
				existentElements.add(needComment ? commentElement(e) : e.createCopy());
			}
		}
		for (Node e : existentElements) {
			context.add(e);
		}
	}
	
	private void appendSqlMapGenerators(MybatisConfigFileGenerator configFileGenerator) {
		appendGenerators(configFileGenerator.getSqlMapGeneratorElements(), sqlMapGenerators);
	}
	
	private void appendJavaModelGenerators(MybatisConfigFileGenerator configFileGenerator) {
		appendGenerators(configFileGenerator.getJavaModelGeneratorElements(), javaModelGenerators);
	}
	
	private void appendJavaClientGenerators(MybatisConfigFileGenerator configFileGenerator) {
		appendGenerators(configFileGenerator.getJavaClientGeneratorElements(), javaClientGenerators);
	}
	
	private void appendClasspathEntry(MybatisConfigFileGenerator configFileGenerator) {
		if (classPathEntry == null) {
			root.add(configFileGenerator.getClassPathEntryElement().createCopy());
		} else {
			root.add(classPathEntry);
		}
	}

	private boolean appendContextAndTestIfContinueAppending(MybatisConfigFileGenerator configFileGenerator) {
		if (context == null) {
			root.add(configFileGenerator.getContext().createCopy());
			return false;
		}
		root.add(context);
		return true;
	}
	
	private void appendJavaTypeResolver(MybatisConfigFileGenerator configFileGenerator) {
		if (javaTypeResolver == null) {
			context.add(configFileGenerator.getJavaTypeResolverElement().createCopy());
		} else {
			context.add(javaTypeResolver);
		}
	}
	
	private void appendJdbcConnection(MybatisConfigFileGenerator configFileGenerator) {
		if (jdbcConnection == null) {
			context.add(configFileGenerator.getJdbConnectionElement().createCopy());
		} else {
			context.add(jdbcConnection);
		}
	}
	
	private void appendTables(MybatisConfigFileGenerator configFileGenerator) {
		Set<Element> newTables = configFileGenerator.getTableElements();
		if (newTables != null && !newTables.isEmpty()) {
			for (Element table: newTables) {
				if (isTableCommented(table)) {
					continue;
				}
				if (!hasTable(table)) {
					tables.add(table.createCopy());
				}
			}
		}
		for (Node e: tables) {
			context.add(e);
		}
	}
	
	private void appendRenamePlugin(MybatisConfigFileGenerator configFileGenerator) {
		if (pluginElement == null) {
			context.add(configFileGenerator.getJdbConnectionElement().createCopy());
		} else {
			context.add(pluginElement);
		}
	}
	
	private void appendPagingAndLockPlugin(MybatisConfigFileGenerator configFileGenerator) {
		if (pagingAndLockElement == null) {
			context.add(configFileGenerator.getPagingAndLockElement().createCopy());
		} else {
			context.add(pagingAndLockElement);
		}
	}
	
	
	/**
	 * Under some circumstances, we might find multiple dao/domain layers, so it's necessary
	 * to merge generated elements. If this existed config file does not have the element in the new one,
	 * a copy is issued.
	 * @param configFileGenerator
	 * @return the merged config file in string.
	 */
	public String mergeGeneratedConfigAndGetXmlString(MybatisConfigFileGenerator configFileGenerator) {
		try {
			appendClasspathEntry(configFileGenerator);
			if (appendContextAndTestIfContinueAppending(configFileGenerator)) {
				appendRenamePlugin(configFileGenerator);
				appendPagingAndLockPlugin(configFileGenerator);
				appendJdbcConnection(configFileGenerator);
				appendJavaTypeResolver(configFileGenerator);
				appendJavaModelGenerators(configFileGenerator);
				appendSqlMapGenerators(configFileGenerator);
				appendJavaClientGenerators(configFileGenerator);
				appendTables(configFileGenerator);
			}
			return toXmlString();
		} catch (IOException e) {
			throw new ProjectException("Failed to merge generated xml into existent xml.");
		}
	}

	
	@Override
	void  customSAXReader(SAXReader saxReader ) {
		saxReader.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				if (systemId.contains("mybatis-generator-config_1_0.dtd")) {
					return new InputSource(new StringReader(DTD)); 
				}
				return null;
			}
		});
	}
	
	
	/*
	 * Each generator set can only has one valid element, while others
	 * need to be commented out, to enable mybatis-generator to work properly.
	 */
	private Element findAcitveElement(Set<Node> generators, String name) {
		if (generators == null || generators.isEmpty()) {
			throw new InvalidMybatisGeneratorConfigException(
					String.format("There is no %s configured, please set the element and re-run.", name));
		}
		Iterator<Node> iterator =  generators.iterator();
		for (int i = 0; iterator.hasNext(); ) {
			Node node = iterator.next();
			i += node instanceof Element? 1 : 0;
			if (i > 1) {
				throw new InvalidMybatisGeneratorConfigException(
					String.format("More than one %s configured, please remove unintentional ones and re-run.", name));
			}
		}
		iterator =  generators.iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();
			if (!(node instanceof Element)) {
				continue;
			}
			return (Element)node;
		}
		throw new InvalidMybatisGeneratorConfigException("Should not happen.");
	}

	/*
	 * a.b.c + /user/test -> /user/test/a/b/c
	 */
	private String glueTargetPackageToTargetProject(Set<Node> generators, String name) {
		Element element = findAcitveElement(generators, name);
		String packageName = element.attributeValue("targetPackage");
		String targetProject = element.attributeValue("targetProject");
		return targetProject + "/" + packageName.replace(".", "/");
	}

	@Override
	public String getDaoDirPath() {
		return glueTargetPackageToTargetProject(javaClientGenerators, CLIENT_GENERATOR_TAG);
	}

	@Override
	public String getDomainDirPath() {
		return glueTargetPackageToTargetProject(javaModelGenerators, MODEL_GENERATOR_TAG);
	}

	@Override
	public String getCriteriaDirPath() {
		String daoPath =  glueTargetPackageToTargetProject(javaModelGenerators, MODEL_GENERATOR_TAG);
		return daoPath + "/criteria";
	}

	@Override
	public String getConfigeFilename() {
		return MybatisConfigFileGenerator.CONFIG_FILENAME;
	}

	@Override
	public String getPackageNameOfDomains() {
		Element element = findAcitveElement(javaModelGenerators, MODEL_GENERATOR_TAG);
		return element.attributeValue("targetPackage");
	}

	@Override
	public String getMapperDirPath() {
		return glueTargetPackageToTargetProject(sqlMapGenerators, SQLMAP_GENERATOR_TAG);
	}

	@Override
	public String getPackageNameOfJavaMappers() {
		Element element = findAcitveElement(javaClientGenerators, CLIENT_GENERATOR_TAG);
		return element.attributeValue("targetPackage");
	}
}
