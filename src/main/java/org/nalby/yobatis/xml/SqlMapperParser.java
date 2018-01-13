package org.nalby.yobatis.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nalby.yobatis.exception.InvalidMapperException;
import org.nalby.yobatis.util.Expect;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SqlMapperParser extends AbstractXmlParser {
	private final static String DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + 
			"<!--\n" + 
			"\n" + 
			"       Copyright 2009-2017 the original author or authors.\n" + 
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
			"<!ELEMENT mapper (cache-ref | cache | resultMap* | parameterMap* | sql* | insert* | update* | delete* | select* )+>\n" + 
			"<!ATTLIST mapper\n" + 
			"namespace CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT cache-ref EMPTY>\n" + 
			"<!ATTLIST cache-ref\n" + 
			"namespace CDATA #REQUIRED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT cache (property*)>\n" + 
			"<!ATTLIST cache\n" + 
			"type CDATA #IMPLIED\n" + 
			"eviction CDATA #IMPLIED\n" + 
			"flushInterval CDATA #IMPLIED\n" + 
			"size CDATA #IMPLIED\n" + 
			"readOnly CDATA #IMPLIED\n" + 
			"blocking CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT parameterMap (parameter+)?>\n" + 
			"<!ATTLIST parameterMap\n" + 
			"id CDATA #REQUIRED\n" + 
			"type CDATA #REQUIRED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT parameter EMPTY>\n" + 
			"<!ATTLIST parameter\n" + 
			"property CDATA #REQUIRED\n" + 
			"javaType CDATA #IMPLIED\n" + 
			"jdbcType CDATA #IMPLIED\n" + 
			"mode (IN | OUT | INOUT) #IMPLIED\n" + 
			"resultMap CDATA #IMPLIED\n" + 
			"scale CDATA #IMPLIED\n" + 
			"typeHandler CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT resultMap (constructor?,id*,result*,association*,collection*, discriminator?)>\n" + 
			"<!ATTLIST resultMap\n" + 
			"id CDATA #REQUIRED\n" + 
			"type CDATA #REQUIRED\n" + 
			"extends CDATA #IMPLIED\n" + 
			"autoMapping (true|false) #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT constructor (idArg*,arg*)>\n" + 
			"\n" + 
			"<!ELEMENT id EMPTY>\n" + 
			"<!ATTLIST id\n" + 
			"property CDATA #IMPLIED\n" + 
			"javaType CDATA #IMPLIED\n" + 
			"column CDATA #IMPLIED\n" + 
			"jdbcType CDATA #IMPLIED\n" + 
			"typeHandler CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT result EMPTY>\n" + 
			"<!ATTLIST result\n" + 
			"property CDATA #IMPLIED\n" + 
			"javaType CDATA #IMPLIED\n" + 
			"column CDATA #IMPLIED\n" + 
			"jdbcType CDATA #IMPLIED\n" + 
			"typeHandler CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT idArg EMPTY>\n" + 
			"<!ATTLIST idArg\n" + 
			"javaType CDATA #IMPLIED\n" + 
			"column CDATA #IMPLIED\n" + 
			"jdbcType CDATA #IMPLIED\n" + 
			"typeHandler CDATA #IMPLIED\n" + 
			"select CDATA #IMPLIED\n" + 
			"resultMap CDATA #IMPLIED\n" + 
			"name CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT arg EMPTY>\n" + 
			"<!ATTLIST arg\n" + 
			"javaType CDATA #IMPLIED\n" + 
			"column CDATA #IMPLIED\n" + 
			"jdbcType CDATA #IMPLIED\n" + 
			"typeHandler CDATA #IMPLIED\n" + 
			"select CDATA #IMPLIED\n" + 
			"resultMap CDATA #IMPLIED\n" + 
			"name CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT collection (constructor?,id*,result*,association*,collection*, discriminator?)>\n" + 
			"<!ATTLIST collection\n" + 
			"property CDATA #REQUIRED\n" + 
			"column CDATA #IMPLIED\n" + 
			"javaType CDATA #IMPLIED\n" + 
			"ofType CDATA #IMPLIED\n" + 
			"jdbcType CDATA #IMPLIED\n" + 
			"select CDATA #IMPLIED\n" + 
			"resultMap CDATA #IMPLIED\n" + 
			"typeHandler CDATA #IMPLIED\n" + 
			"notNullColumn CDATA #IMPLIED\n" + 
			"columnPrefix CDATA #IMPLIED\n" + 
			"resultSet CDATA #IMPLIED\n" + 
			"foreignColumn CDATA #IMPLIED\n" + 
			"autoMapping (true|false) #IMPLIED\n" + 
			"fetchType (lazy|eager) #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT association (constructor?,id*,result*,association*,collection*, discriminator?)>\n" + 
			"<!ATTLIST association\n" + 
			"property CDATA #REQUIRED\n" + 
			"column CDATA #IMPLIED\n" + 
			"javaType CDATA #IMPLIED\n" + 
			"jdbcType CDATA #IMPLIED\n" + 
			"select CDATA #IMPLIED\n" + 
			"resultMap CDATA #IMPLIED\n" + 
			"typeHandler CDATA #IMPLIED\n" + 
			"notNullColumn CDATA #IMPLIED\n" + 
			"columnPrefix CDATA #IMPLIED\n" + 
			"resultSet CDATA #IMPLIED\n" + 
			"foreignColumn CDATA #IMPLIED\n" + 
			"autoMapping (true|false) #IMPLIED\n" + 
			"fetchType (lazy|eager) #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT discriminator (case+)>\n" + 
			"<!ATTLIST discriminator\n" + 
			"column CDATA #IMPLIED\n" + 
			"javaType CDATA #REQUIRED\n" + 
			"jdbcType CDATA #IMPLIED\n" + 
			"typeHandler CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT case (constructor?,id*,result*,association*,collection*, discriminator?)>\n" + 
			"<!ATTLIST case\n" + 
			"value CDATA #REQUIRED\n" + 
			"resultMap CDATA #IMPLIED\n" + 
			"resultType CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT property EMPTY>\n" + 
			"<!ATTLIST property\n" + 
			"name CDATA #REQUIRED\n" + 
			"value CDATA #REQUIRED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT typeAlias EMPTY>\n" + 
			"<!ATTLIST typeAlias\n" + 
			"alias CDATA #REQUIRED\n" + 
			"type CDATA #REQUIRED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT select (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST select\n" + 
			"id CDATA #REQUIRED\n" + 
			"parameterMap CDATA #IMPLIED\n" + 
			"parameterType CDATA #IMPLIED\n" + 
			"resultMap CDATA #IMPLIED\n" + 
			"resultType CDATA #IMPLIED\n" + 
			"resultSetType (FORWARD_ONLY | SCROLL_INSENSITIVE | SCROLL_SENSITIVE) #IMPLIED\n" + 
			"statementType (STATEMENT|PREPARED|CALLABLE) #IMPLIED\n" + 
			"fetchSize CDATA #IMPLIED\n" + 
			"timeout CDATA #IMPLIED\n" + 
			"flushCache (true|false) #IMPLIED\n" + 
			"useCache (true|false) #IMPLIED\n" + 
			"databaseId CDATA #IMPLIED\n" + 
			"lang CDATA #IMPLIED\n" + 
			"resultOrdered (true|false) #IMPLIED\n" + 
			"resultSets CDATA #IMPLIED \n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT insert (#PCDATA | selectKey | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST insert\n" + 
			"id CDATA #REQUIRED\n" + 
			"parameterMap CDATA #IMPLIED\n" + 
			"parameterType CDATA #IMPLIED\n" + 
			"timeout CDATA #IMPLIED\n" + 
			"flushCache (true|false) #IMPLIED\n" + 
			"statementType (STATEMENT|PREPARED|CALLABLE) #IMPLIED\n" + 
			"keyProperty CDATA #IMPLIED\n" + 
			"useGeneratedKeys (true|false) #IMPLIED\n" + 
			"keyColumn CDATA #IMPLIED\n" + 
			"databaseId CDATA #IMPLIED\n" + 
			"lang CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT selectKey (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST selectKey\n" + 
			"resultType CDATA #IMPLIED\n" + 
			"statementType (STATEMENT|PREPARED|CALLABLE) #IMPLIED\n" + 
			"keyProperty CDATA #IMPLIED\n" + 
			"keyColumn CDATA #IMPLIED\n" + 
			"order (BEFORE|AFTER) #IMPLIED\n" + 
			"databaseId CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT update (#PCDATA | selectKey | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST update\n" + 
			"id CDATA #REQUIRED\n" + 
			"parameterMap CDATA #IMPLIED\n" + 
			"parameterType CDATA #IMPLIED\n" + 
			"timeout CDATA #IMPLIED\n" + 
			"flushCache (true|false) #IMPLIED\n" + 
			"statementType (STATEMENT|PREPARED|CALLABLE) #IMPLIED\n" + 
			"keyProperty CDATA #IMPLIED\n" + 
			"useGeneratedKeys (true|false) #IMPLIED\n" + 
			"keyColumn CDATA #IMPLIED\n" + 
			"databaseId CDATA #IMPLIED\n" + 
			"lang CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT delete (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST delete\n" + 
			"id CDATA #REQUIRED\n" + 
			"parameterMap CDATA #IMPLIED\n" + 
			"parameterType CDATA #IMPLIED\n" + 
			"timeout CDATA #IMPLIED\n" + 
			"flushCache (true|false) #IMPLIED\n" + 
			"statementType (STATEMENT|PREPARED|CALLABLE) #IMPLIED\n" + 
			"databaseId CDATA #IMPLIED\n" + 
			"lang CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!-- Dynamic -->\n" + 
			"\n" + 
			"<!ELEMENT include (property+)?>\n" + 
			"<!ATTLIST include\n" + 
			"refid CDATA #REQUIRED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT bind EMPTY>\n" + 
			"<!ATTLIST bind\n" + 
			" name CDATA #REQUIRED\n" + 
			" value CDATA #REQUIRED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT sql (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST sql\n" + 
			"id CDATA #REQUIRED\n" + 
			"lang CDATA #IMPLIED\n" + 
			"databaseId CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT trim (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST trim\n" + 
			"prefix CDATA #IMPLIED\n" + 
			"prefixOverrides CDATA #IMPLIED\n" + 
			"suffix CDATA #IMPLIED\n" + 
			"suffixOverrides CDATA #IMPLIED\n" + 
			">\n" + 
			"<!ELEMENT where (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ELEMENT set (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"\n" + 
			"<!ELEMENT foreach (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST foreach\n" + 
			"collection CDATA #REQUIRED\n" + 
			"item CDATA #IMPLIED\n" + 
			"index CDATA #IMPLIED\n" + 
			"open CDATA #IMPLIED\n" + 
			"close CDATA #IMPLIED\n" + 
			"separator CDATA #IMPLIED\n" + 
			">\n" + 
			"\n" + 
			"<!ELEMENT choose (when* , otherwise?)>\n" + 
			"<!ELEMENT when (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST when\n" + 
			"test CDATA #REQUIRED\n" + 
			">\n" + 
			"<!ELEMENT otherwise (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"\n" + 
			"<!ELEMENT if (#PCDATA | include | trim | where | set | foreach | choose | if | bind)*>\n" + 
			"<!ATTLIST if\n" + 
			"test CDATA #REQUIRED\n" + 
			">\n" + 
			"\n" + 
			"\n" + 
			"";
	private Set<String> idSet = new HashSet<String>();
	public SqlMapperParser(InputStream inputStream)
			throws DocumentException, IOException {
		super(inputStream, "mapper");
		List<Element> thisElements = document.getRootElement().elements();
		for (Element element : thisElements)  {
			if (element.attributeValue("id") == null) {
				continue;
			}
			if (!idSet.add(element.attributeValue("id"))) {
				throw new InvalidMapperException("Duplicated id: " + element.attributeValue("id"));
			}
		}
	}
	
	public void merge(SqlMapperParser toMerge) {
		for (String thatId: toMerge.idSet) {
			if (idSet.contains(thatId)) {
				throw new InvalidMapperException("id '" + thatId + "' is preserved.");
			}
		}
		Iterator<Node> iterator = toMerge.document.getRootElement().nodeIterator();
		Element thisRoot = document.getRootElement();
		while (iterator.hasNext()) {
			Node node = iterator.next();
			iterator.remove();
			thisRoot.add(node.detach());
		}
	}
	
	public static SqlMapperParser fromString(String content) {
		Expect.notNull(content, "content must not be null.");
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
			return new SqlMapperParser(inputStream);
		} catch (Exception e) {
			throw new InvalidMapperException(e);
		}
	}

	@Override
	protected void customSAXReader(SAXReader saxReader ) {
		saxReader.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				if (systemId.contains("mybatis-3-mapper.dtd")) {
					return new InputSource(new StringReader(DTD)); 
				}
				return null;
			}
		});
	}

}
