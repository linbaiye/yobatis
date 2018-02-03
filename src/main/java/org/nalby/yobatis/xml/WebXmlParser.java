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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.TextUtil;

public class WebXmlParser extends AbstractXmlParser {

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
		
	/**
	 * Construct {@code Document} from the {@code inputStream},
	 * the caller needs to be responsible for closing the stream.
	 * @param inputStream the input stream.
	 * @throws IOException if the input stream is invalid.
	 * @throws DocumentException if the input stream does not represent a valid xml document.
	 */
	public WebXmlParser(InputStream inputStream) throws IOException, DocumentException {
		super(inputStream, WEB_APP_TAG);
	}


	private List<Element> selectElements(String elementName) {
		Element root = document.getRootElement();
		return root.elements(elementName);
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
	 * @throws UnsupportedProjectException if multiple locations or no location detected.
	 */
	private String getAppConfigLocation() {
		String result = null;
		for (Element it: selectElements(CONTEXT_PARAM_TAG)) {
			String paramValue = getParamValue(it);
			if (TextUtil.isEmpty(paramValue)) {
				continue;
			}
			if (result != null) {
				throw new UnsupportedProjectException("Multiple contextConfigLocations detected.");
			}
			result = paramValue;
		}
		return result;
	}

	
	/**
	 * Get servlet's config location.
	 * @return the value of config location.
	 * @throws UnsupportedProjectException if multiple or no config locations found.
	 */
	private String getServletConfigLocation() {
		String result = null;
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
			for (Element initParam: initParamElements) {
				String value = getParamValue(initParam);
				if (TextUtil.isEmpty(value)) {
					continue;
				}
				if (result != null) {
					throw new UnsupportedProjectException("Multiple contextConfigLocations found.");
				}
				result = value;
			}
		}
		return result;
	}
	
	/**
	 * Get spring's configuration files.
	 * @return configuration files if found.
	 */
	public Set<String> getSpringInitParamValues() {
		Set<String> result = new HashSet<String>();
		String tmp = getAppConfigLocation();
		if (tmp != null) {
			result.add(tmp);
		}
		tmp = getServletConfigLocation();
		if (tmp != null) {
			result.add(tmp);
		}
		return result;
	}
	
}
