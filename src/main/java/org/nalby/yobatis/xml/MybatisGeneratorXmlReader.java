package org.nalby.yobatis.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlCreator;
import org.nalby.yobatis.mybatis.MybatiGeneratorAnalyzer;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MybatisGeneratorXmlReader extends AbstractXmlParser implements MybatiGeneratorAnalyzer {
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
	

	private DocumentFactory documentFactory  = DocumentFactory.getInstance();

	private List<Element> plugins = new LinkedList<>();
	
	private List<Element> javaModelGenerators = new LinkedList<>();

	private List<Element> sqlMapGenerators = new LinkedList<>();

	private List<Element> javaClientGenerators = new LinkedList<>();

	private List<Element> tables = new LinkedList<>();
	
	private List<Element> commentedElements;
	
	public final static String CLASS_PATH_ENTRY_TAG = "classPathEntry";
	public final static String MODEL_GENERATOR_TAG = "javaModelGenerator";
	public final static String SQLMAP_GENERATOR_TAG = "sqlMapGenerator";
	public final static String CLIENT_GENERATOR_TAG = "javaClientGenerator";
	public final static String TABLE_TAG = "table";
	public final static String ROOT_TAG = "generatorConfiguration";
	public final static String CONTEXT_ID = "yobatis";
	public final static String PLUGIN_TAG = "plugin";
	public final static String TARGET_RUNTIME = "MyBatis3";

	public MybatisGeneratorXmlReader(InputStream inputStream) throws DocumentException, IOException {
		super(inputStream, ROOT_TAG);
		root = document.getRootElement();
		loadClasspathEntry();
		loadContext();
		if (context != null) {
			loadCommentedElements();
			loadJdbcConnection();
			loadJavaTypeResolver();
			loadElements(PLUGIN_TAG, plugins);
			loadElements(MODEL_GENERATOR_TAG, javaModelGenerators);
			loadElements(SQLMAP_GENERATOR_TAG, sqlMapGenerators);
			loadElements(CLIENT_GENERATOR_TAG, javaClientGenerators);
			loadElements(TABLE_TAG, tables);
		}
		document.remove(root);
		root = documentFactory.createElement(ROOT_TAG);
		document.setRootElement(root);
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
	
	private Element findPluginElement(List<Element> elements, Element target) {
		for (Element element : elements) {
			if (!PLUGIN_TAG.equals(element.getName())) {
				continue;
			}
			String typeAttr = target.attributeValue("type");
			if (typeAttr != null && 
				typeAttr.equals(element.attributeValue("type"))) {
				return element;
			}
		}
		return null;
	}
	
	private void loadElements(String tag, List<Element> dst) {
		for (Element element : context.elements(tag)) {
			dst.add(element.createCopy());
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
	

	private void loadCommentedElements()  {
		String text = null;
		for (Iterator<Node> iterator = context.nodeIterator(); iterator.hasNext(); ) {
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
	
	
	private void loadClasspathEntry() {
		classPathEntry = root.element(CLASS_PATH_ENTRY_TAG);	
		if (classPathEntry != null) {
			classPathEntry.detach();
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

	
	private Element findTable(List<Element> elements, Element table) {
		for (Element e: elements) {
			if (!"table".equals(e.getName())) {
				continue;
			}
			String name = e.attributeValue("tableName");
			String schema = e.attributeValue("schema");
			if ((name != null && name.equals(table.attributeValue("tableName"))) &&
				(schema != null && schema.equals(table.attributeValue("schema")))) {
				return e;
			}
		}
		return null;
	}

	private boolean hasTable(Element table) {
		return findTable(tables, table) != null;
	}
	
	private Comment commentElement(Element e) {
		String str = e.asXML();
		str = str.replaceFirst("^<", "");
		str = str.replaceFirst("/>$", "");
		return documentFactory.createComment(str);
	}

	private void mergeGenerators(Set<Element> generatedOnes, List<Element> currentOnes) {
		if (currentOnes.isEmpty()) {
			currentOnes.addAll(generatedOnes);
		}
		for (Element e : currentOnes) {
			context.add(e.createCopy());
		}
	}
	
	private void mergeSqlMapGenerators(MybatisGeneratorXmlCreator configFileGenerator) {
		mergeGenerators(configFileGenerator.getSqlMapGeneratorElements(), sqlMapGenerators);
	}
	
	private void mergeJavaModelGenerators(MybatisGeneratorXmlCreator configFileGenerator) {
		mergeGenerators(configFileGenerator.getJavaModelGeneratorElements(), javaModelGenerators);
	}
	
	private void mergeJavaClientGenerators(MybatisGeneratorXmlCreator configFileGenerator) {
		mergeGenerators(configFileGenerator.getJavaClientGeneratorElements(), javaClientGenerators);
	}
	
	private void mergeClasspathEntry(MybatisGeneratorXmlCreator configFileGenerator) {
		if (classPathEntry == null) {
			root.add(configFileGenerator.getClassPathEntryElement().createCopy());
		} else {
			root.add(classPathEntry);
		}
	}

	private boolean mergeContext(MybatisGeneratorXmlCreator configFileGenerator) {
		if (context == null) {
			root.add(configFileGenerator.getContext().createCopy());
			return false;
		}
		context = root.addElement("context");
		context.addAttribute("id", MybatiGeneratorAnalyzer.DEFAULT_CONTEXT_ID);
		context.addAttribute("targetRuntime", MybatiGeneratorAnalyzer.TARGET_RUNTIME);
		return true;
	}
	
	private void mergeJavaTypeResolver(MybatisGeneratorXmlCreator configFileGenerator) {
		if (javaTypeResolver == null) {
			context.add(configFileGenerator.getJavaTypeResolverElement().createCopy());
		} else {
			context.add(javaTypeResolver);
		}
	}
	
	private void mergeJdbcConnection(MybatisGeneratorXmlCreator configFileGenerator) {
		if (jdbcConnection == null) {
			context.add(configFileGenerator.getJdbConnectionElement().createCopy());
		} else {
			context.add(jdbcConnection);
		}
	}

	
	private void mergeTables(MybatisGeneratorXmlCreator configFileGenerator) {
		for (Element current: tables) {
			context.add(current);
		}
		for (Element newTable : configFileGenerator.getTableElements()) {
			if (hasTable(newTable)) {
				continue;
			}
			Element commented = findTable(commentedElements, newTable);
			if (commented != null) {
				context.add(commentElement(commented));
			} else {
				context.add(newTable);
			}
		}
	}
	
	//TODO: Still need to cope with artificially added plug-ins.
	private void mergePlugins(MybatisGeneratorXmlCreator configFileGenerator) {
		Element pluginElement = configFileGenerator.getPluginElement();
		Element currentPlugin = findPluginElement(plugins, pluginElement);
		if (currentPlugin == null) {
			context.add(pluginElement.createCopy());
		} else {
			context.add(currentPlugin.createCopy());
		}

		pluginElement = configFileGenerator.getCriteriaPluginElement();
		currentPlugin = findPluginElement(plugins, pluginElement);
		if (currentPlugin == null) {
			Element commentedPlugin = findPluginElement(commentedElements, pluginElement);
			if (commentedPlugin == null) {
				context.add(pluginElement.createCopy());
			} else {
				context.add(commentElement(commentedPlugin));
			}
		} else {
			context.add(currentPlugin.createCopy());
		}
	}
	
	/**
	 * Preserve manually edited elements. 
	 * @param configFileGenerator
	 */
	public void mergeGeneratedConfig(MybatisGeneratorXmlCreator configFileGenerator) {
		mergeClasspathEntry(configFileGenerator);
		if (mergeContext(configFileGenerator)) {
			mergePlugins(configFileGenerator);
			mergeJdbcConnection(configFileGenerator);
			mergeJavaTypeResolver(configFileGenerator);
			mergeJavaModelGenerators(configFileGenerator);
			mergeSqlMapGenerators(configFileGenerator);
			mergeJavaClientGenerators(configFileGenerator);
			mergeTables(configFileGenerator);
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
	
	
	
	private void assertHasSingleElement(List<Element> elements, String name) {
		if (elements.isEmpty() ) {
			throw new InvalidMybatisGeneratorConfigException(
				String.format("There is no %s configured, please set the element and re-run.", name));
		} else if (elements.size() > 1)  {
			throw new InvalidMybatisGeneratorConfigException(
				String.format("More than one %s configured, please remove unintentional ones and re-run.", name));
		}
	}

	
	/*
	 * a.b.c + /user/test -> /user/test/a/b/c
	 */
	private String buildGeneratorPath(List<Element> elements, String name) {
		assertHasSingleElement(elements, name);
		Element element = elements.get(0);
		String packageName = element.attributeValue("targetPackage");
		String targetProject = element.attributeValue("targetProject");
		return targetProject + "/" + packageName.replace(".", "/");
	}


	@Override
	public String getDaoDirPath() {
		return buildGeneratorPath(javaClientGenerators, CLIENT_GENERATOR_TAG);
	}

	@Override
	public String getDomainDirPath() {
		return buildGeneratorPath(javaModelGenerators, MODEL_GENERATOR_TAG);
	}

	@Override
	public String getCriteriaDirPath() {
		return getDomainDirPath() + "/criteria";
	}


	private String getTargetPackage(List<Element> elements, String tag) {
		assertHasSingleElement(elements, tag);
		Element element = javaModelGenerators.get(0);
		return element.attributeValue("targetPackage");
	}

	@Override
	public String getPackageNameOfDomains() {
		return getTargetPackage(javaModelGenerators, MODEL_GENERATOR_TAG);
	}

	@Override
	public String getXmlMapperDirPath() {
		return buildGeneratorPath(sqlMapGenerators, SQLMAP_GENERATOR_TAG);
	}

	@Override
	public String getPackageNameOfJavaMappers() {
		return getTargetPackage(javaClientGenerators, CLIENT_GENERATOR_TAG);
	}

	@Override
	public String asXmlText() {
		try {
			return toXmlString(document);
		} catch (IOException e){
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
}
