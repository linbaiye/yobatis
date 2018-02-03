package org.nalby.yobatis.xml;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.Test;

public class AbstractXmlParserTests {
	
	private class XmlHelper extends AbstractXmlParser  {
		public XmlHelper(InputStream inputStream) throws DocumentException, IOException {
			super(inputStream, "context");
		}
		public Element getRoot() {
			return document.getRootElement();
		}
	}
	
	private Element build(String xml) {
		try {
			Element tmp = new XmlHelper(new ByteArrayInputStream(xml.getBytes())).getRoot();
			assertTrue(tmp != null);
			return tmp;
		} catch (DocumentException | IOException e) {
			throw new IllegalAccessError("bad xml.");
		}
	}
	
	
	@Test
	public void onlyTextComments() {
		String xml = "<context><!--test--></context>";
		List<Element> elements = AbstractXmlParser.loadCommentedElements(build(xml));
		assertTrue(elements.isEmpty());
	}

	@Test
	public void oneComment() {
		String xml = "<context><!--test/--></context>";
		List<Element> elements = AbstractXmlParser.loadCommentedElements(build(xml));
		assertTrue(elements.size() == 1);
		assertTrue(elements.get(0).getName().equals("test"));
	}
	
	@Test
	public void mixed() {
		String xml = "<context><test3/><!--test/--><!--test1/><test2/--></context>";
		List<Element> elements = AbstractXmlParser.loadCommentedElements(build(xml));
		assertTrue(elements.size() == 3);
	}
	
	@Test
	public void comment() {
		String xml = "<context><test3/></context>";
		Element context = build(xml);
		Element test = context.element("test3");
		assertTrue(AbstractXmlParser.commentElement(test).asXML().equals("<!--test3-->"));
	}

}
