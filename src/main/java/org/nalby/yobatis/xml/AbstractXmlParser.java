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
package org.nalby.yobatis.xml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.nalby.yobatis.util.Expect;

public abstract class AbstractXmlParser {
	
	private static final int MAX_STREAM_SIZE = 5 * 1024 * 1024;

	protected Document document;

	public AbstractXmlParser(InputStream inputStream, String rootElmentTag) throws DocumentException, IOException {
		Expect.asTrue(inputStream != null
				&& inputStream.available() <= MAX_STREAM_SIZE
				&& inputStream.available() > 0,
				"invalid input stream");
		Expect.notNull(rootElmentTag, "root element tag is null.");
		SAXReader saxReader = new SAXReader();
		saxReader.setValidation(false);
		customSAXReader(saxReader);
	
		document = saxReader.read(new BufferedInputStream(inputStream));
		if (document == null || document.getRootElement() == null
				|| !rootElmentTag.equals(document.getRootElement().getName())) {
			throw new DocumentException("Unpexpected document.");
		}
	}
	
	protected void customSAXReader(SAXReader saxReader) {}
	
	
	private static String removeBlankLines(String content) throws IOException {
		StringReader stringReader = new StringReader(content);
		BufferedReader reader = new BufferedReader(stringReader);
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() != 0) {
				builder.append(line);
				builder.append("\n");
			}
		}
		return builder.toString();
	}
	
	public static String toXmlString(Document document) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setTrimText(false);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos, true, "utf-8");
	    XMLWriter writer = new XMLWriter(ps, format);
	    writer.write(document);
		String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		ps.close();
		return removeBlankLines(content);
	}
	
	public String toXmlString() throws IOException {
		return AbstractXmlParser.toXmlString(document);
	}
	
	private static Document buildDoc(String text) {
		SAXReader saxReader = new SAXReader();
		saxReader.setValidation(false);
		saxReader.setStripWhitespaceText(false);
		try  {
			return saxReader.read(new ByteArrayInputStream(("<rootDoc>" + text + "</rootDoc>").getBytes()));
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<Element> convertToElements(String text) {
		List<Element> commentedElements = null;
		Document doc = buildDoc(text);
		if (doc != null) {
			commentedElements = doc.getRootElement().elements();
		}
		if (commentedElements == null) {
			commentedElements = Collections.EMPTY_LIST;
		}
		return commentedElements;
	}
	
	private static boolean isCommentedElement(String text) {
		if (buildDoc(text) != null) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Uncomment commented elements contained by parent.
	 * @param parent 
	 * @return elements after uncommenting, or an empty list.
	 */
	public static List<Element> loadCommentedElements(Element parent)  {
		Expect.notNull(parent, "parent must not be emtpy.");
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
		return convertToElements(text);
	}
	
	public static Comment commentElement(Element e) {
		Expect.notNull(e, "e must not be null.");
		String str = e.asXML();
		str = str.replaceFirst("^<", "");
		str = str.replaceFirst(">$", "");
		return DocumentFactory.getInstance().createComment(str);
	}

}