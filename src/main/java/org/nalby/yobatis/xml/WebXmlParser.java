package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nalby.yobatis.util.Expect;

public class WebXmlParser {

	private static final int MAX_STREAM_SIZE = 4 * 1024 * 1024;
	private static final String CONTEXT_PARAM_TAG = "context-param";
	private static final String PARAM_NAME_TAG = "param-name";
	private static final String TARGET_PARAM_NAME_VALUE = "contextConfigLocation";
	private static final String PARAM_VALUE_TAG = "param-value";
	private static final String WEB_APP_TAG = "web-app";

	private Document document;

	/**
	 * Construct {@code Document} from the {@code inputStream},
	 * the caller needs to be responsible for closing the stream.
	 * @param inputStream the input stream.
	 * @throws IOException if the input stream is invalid.
	 * @throws DocumentException if the input stream does not represent a valid xml document.
	 */
	public WebXmlParser(InputStream inputStream) throws IOException, DocumentException {
		Expect.asTrue(inputStream != null
				&& inputStream.available() <= MAX_STREAM_SIZE
				&& inputStream.available() > 0,
				"invalid input stream");
		SAXReader saxReader = new SAXReader();
		document = saxReader.read(inputStream);
		if (document == null || document.getRootElement() == null
				|| !WEB_APP_TAG.equals(document.getRootElement().getName())) {
			throw new DocumentException("Unpexpected document.");
		}
	}

	
	private List<Element> selectElements(String elementName) {
		Element root = document.getRootElement();
		List<Element> result = new LinkedList<Element>();
		for (Iterator<Element> it = root.elementIterator(); it.hasNext(); ) {
			Element element = it.next();
			if (elementName.equals(element.getName())) {
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Get the value of contextConfigLocation.
	 * @return the value of contextConfigLocation or null if not found.
	 * @throws DocumentException if multiple locations detected.
	 */
	public String getAppConfigLocation() throws DocumentException {
		String result = null;
		List<Element> contextParamElements = selectElements(CONTEXT_PARAM_TAG);
		for (Element it: contextParamElements) {
			Element name = it.element(PARAM_NAME_TAG);
			Element value = it.element(PARAM_VALUE_TAG);
			if (name == null || value == null
				|| !TARGET_PARAM_NAME_VALUE.equals(name.getStringValue())) {
				continue;
			}
			if (result != null) {
				throw new DocumentException("Multiple contextConfigLocation detected.");
			}
			result = value.getStringValue();
		}
		return result;
	}
}
