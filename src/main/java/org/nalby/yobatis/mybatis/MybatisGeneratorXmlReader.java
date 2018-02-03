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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.xml.AbstractXmlParser;
import org.xml.sax.InputSource;

/**
 * MybatisGeneratorXmlReader reads the existent configuration file of MyBatis Generator
 * and merges newly generated one. 
 * 
 * @author Kyle Lin
 *
 */
public class MybatisGeneratorXmlReader extends AbstractXmlParser implements MybatisGenerator {
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
	
	private DocumentFactory documentFactory  = DocumentFactory.getInstance();

	private List<MybatisGeneratorContext> contexts;
	
	private List<MybatisGeneratorContext> commentedContexts;

	private MybatisGeneratorXmlReader(InputStream inputStream) throws DocumentException, IOException {
		super(inputStream, ROOT_TAG);
		root = document.getRootElement();
		classPathEntry = root.element(CLASS_PATH_ENTRY_TAG);	
		loadContexts(root);
		loadCommentedContexts(root);
		root = documentFactory.createElement(ROOT_TAG);
		document = null;
	}
	
	private void loadCommentedContexts(Element parent) {
		commentedContexts = new ArrayList<>();
		List<Element> tmpList = AbstractXmlParser.loadCommentedElements(parent);
		for (Element tmp : tmpList) {
			if (!tmp.getName().equals("context")) {
				continue;
			}
			try {
				commentedContexts.add(new MybatisGeneratorContext(tmp));
			} catch (Exception e) {

			}
		}
	}
	
	/**
	 * Build {@link MybatisGeneratorXmlReader} from the inputStream.
	 * @param inputStream
	 * @return an instance of {@link MybatisGeneratorXmlReader}.
	 * @throws InvalidMybatisGeneratorConfigException if the inputStream is not from a valid xml file.
	 */
	public static MybatisGeneratorXmlReader build(InputStream inputStream)  {
		try {
			return new MybatisGeneratorXmlReader(inputStream);
		} catch (DocumentException | IOException e) {
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
	
	private void loadContexts(Element root) {
		contexts = new LinkedList<>();
		List<Element> thisContexts = root.elements("context");
		for (Element element : thisContexts) {
			contexts.add(new MybatisGeneratorContext(element));
		}
	}
	
	private void mergeClasspathEntry(Element thatClassPathEntry) {
		if (classPathEntry == null) {
			classPathEntry = thatClassPathEntry;
		}
	}
	
	/**
	 * Drop tables that already configured in existent contexts from generated contexts.
	 * @param thatContexts generated contexts.
	 */
	private void dropExistentTables(List<MybatisGeneratorContext> thatContexts) {
		for (MybatisGeneratorContext thisContext: contexts) {
			for (MybatisGeneratorContext thatContext: thatContexts) {
				thatContext.removeExistentTables(thisContext);
			}
		}
	}
	
	private void mergeContexts(List<MybatisGeneratorContext> thatContexts) {
		dropExistentTables(thatContexts);
		for (MybatisGeneratorContext thisContext: contexts) {
			for (MybatisGeneratorContext thatContext: thatContexts) {
				if (thisContext.idEqualsTo(thatContext)) {
					thisContext.merge(thatContext);
				}
			}
		}
		List<MybatisGeneratorContext> diff = new ArrayList<>();
		for (MybatisGeneratorContext thatContext: thatContexts) {
			boolean found = false;
			for (MybatisGeneratorContext thisContext: contexts) {
				if (thisContext.idEqualsTo(thatContext)) {
					found = true;
				}
			}
			if (!found) {
				diff.add(thatContext);
			}
		}
		contexts.addAll(diff);
	}

	
	/**
	 * Preserve manually edited elements. 
	 * @param configFileGenerator
	 */
	public void mergeGeneratedConfig(MybatisGeneratorXmlCreator configFileGenerator) {
		Expect.notNull(configFileGenerator, "configFileGenerator must not be empty.");
		mergeClasspathEntry(configFileGenerator.getClassPathEntryElement());
		mergeContexts(configFileGenerator.getContexts());
	}

	@Override
	protected void customSAXReader(SAXReader saxReader ) {
		saxReader.setEntityResolver((String publicId, String systemId) -> {
			if (systemId.contains("mybatis-generator-config_1_0.dtd")) {
				return new InputSource(new StringReader(DTD));
			}
			return null;
		});
	}

	@Override
	public String asXmlText() {
		try {
			if (document == null) {
				document = documentFactory.createDocument();
				DocumentType type = documentFactory.createDocType(ROOT_TAG, 
						"-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN",
						"http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd");
				document.setDocType(type);
				if (classPathEntry != null) {
					root.add(classPathEntry.createCopy());
				}
				for (MybatisGeneratorContext context : contexts) {
					if (context.hasTable()) {
						root.add(context.getContext().createCopy());
					}
				}
				for (MybatisGeneratorContext context : commentedContexts) {
					root.add(AbstractXmlParser.commentElement(context.getContext().createCopy()));
				}
				document.setRootElement(root);
			}
			return toXmlString(document);
		} catch (IOException e){
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
}
