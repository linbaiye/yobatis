package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nalby.yobatis.util.Expect;

public class WebXmlParser {

	//The maximum size of the web.xml
	private static final int MAX_STREAM_SIZE = 5 * 1024 * 1024;

	private static final String WEB_APP_TAG = "web-app";

	private static final String CONTEXT_PARAM_TAG = "context-param";

	private static final String INIT_PARAM_TAG = "init-param";

	private static final String PARAM_NAME_TAG = "param-name";

	private static final String PARAM_VALUE_TAG = "param-value";
	
	private static final String TARGET_PARAM_NAME_VALUE = "contextConfigLocation";

	private static final String SERVLET_TAG = "servlet";

	private static final String SERVLET_CLASS_TAG = "servlet-class";
	
	//Fully qualified class name of the spring dispatcher servlet.
	private static final String DISPATCHER_SERVLET_FQCN = "org.springframework.web.servlet.DispatcherServlet";
		
	private static final List<Element> EMPTY_LIST = new ArrayList<Element>(0);

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
		List<Element> result = root.elements(elementName);
		return result == null ? EMPTY_LIST : result;
	}

	
	private String getParamValue(Element container) {
		Element name = container.element(PARAM_NAME_TAG);
		Element value = container.element(PARAM_VALUE_TAG);
		if (name == null || value == null || 
			!TARGET_PARAM_NAME_VALUE.equals(name.getStringValue())) {
			return null;
		}
		return value.getStringValue();
	}
	

	/**
	 * Get the value of contextConfigLocation.
	 * @return the value of contextConfigLocation or null if not found.
	 * @throws DocumentException if multiple locations detected.
	 */
	public String getAppConfigLocation() throws DocumentException {
		String result = null;
		for (Element it: selectElements(CONTEXT_PARAM_TAG)) {
			String paramValue = getParamValue(it);
			if (paramValue == null) {
				continue;
			}
			if (result != null) {
				throw new DocumentException("Multiple contextConfigLocation detected.");
			}
			result = paramValue;
		}
		return result;
	}

	
	/**
	 * Get servlets' config locations.
	 * @return a set which contains all the locations or an empty set if locations not specified.
	 * @throws DocumentException if the same location appears more than once.
	 */
	public Set<String> getServletConfigLocation() throws DocumentException {
		Set<String> result = new HashSet<String>();
		for (Element servletElement: selectElements(SERVLET_TAG)) {
			Element classElement = servletElement.element(SERVLET_CLASS_TAG);
			if (classElement == null || 
				!DISPATCHER_SERVLET_FQCN.equals(classElement.getStringValue())) {
				continue;
			}
			List<Element> initParamElements = servletElement.elements(INIT_PARAM_TAG);
			if (initParamElements == null || initParamElements.isEmpty()) {
				continue;
			}
			int counter = 0;
			for (Element initParam: initParamElements) {
				String value = getParamValue(initParam);
				if (value != null && result.contains(value)) {
					throw new DocumentException("multiple contextConfigLocation " + value + " in servlet config.");
				}
				if (value != null) {
					result.add(value);
					++counter;
				}
				if (counter > 1) {
					throw new DocumentException("multiple contextConfigLocation detected in the same servlet config.");
				}
			}
		}
		return result;
	}
}
