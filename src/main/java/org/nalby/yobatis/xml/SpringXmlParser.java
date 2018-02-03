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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.nalby.yobatis.util.TextUtil;
public class SpringXmlParser extends AbstractXmlParser {

	private static final String BEANS_TAG = "beans";

	private static final String[] DATASOURCE_CLASSES = {
			"com.alibaba.druid.pool.DruidDataSource",
			"org.apache.commons.dbcp.BasicDataSource" };

	private static final String P_NAMESPACE = "http://www.springframework.org/schema/p";
	private static final String CONTEXT_NAMESPACE = "http://www.springframework.org/schema/context";
	
	private Set<String> propertiesFileLocations = null;

	private Set<String> importLocations = null;

	public SpringXmlParser(InputStream inputStream) throws DocumentException, IOException {
		super(inputStream, BEANS_TAG);
		propertiesFileLocations = new HashSet<String>();
		importLocations = new HashSet<String>();
		loadImportLocations();
		loadPropertieLocationsInContextProperties();
		loadPropertiesLocations();
	}

	
	private void loadPropertieLocationsInContextProperties() {
		Element root = document.getRootElement();
		QName qName = new QName("property-placeholder", new Namespace("context", CONTEXT_NAMESPACE));
		List<Element> elements = root.elements(qName);
		for (Element element : elements) {
			String text = element.attributeValue("location");
			if (TextUtil.isEmpty(text)) {
				continue;
			}
			String vals[] = text.split(",");
			for (String tmp: vals) {
				if (!TextUtil.isEmpty(tmp)) {
					propertiesFileLocations.add(tmp.trim());
				}
			}
		}
	}
	
	private void loadImportLocations() {
		List<Element> importElements = document.getRootElement().elements("import");
		for (Element importElement: importElements) {
			String val = importElement.attributeValue("resource");
			if (!TextUtil.isEmpty(val)) {
				importLocations.add(val.trim());
			}
		}
	}
	
	
	private Element findElementByAttr(List<Element>elements, String attrName, String attrValue) {
		for (Element element : elements) {
			if (attrValue.equals(element.attributeValue(attrName))) {
				return element;
			}
		}
		return null;
	}
	

	/**
	 * Find the property value of a bean.
	 * @param bean the bean element.
	 * @param name the property name.
	 * @return
	 */
	private String findPropertyValue(Element bean, String name) {
		QName qname = new QName(name, new Namespace("p", P_NAMESPACE));
		String val = bean.attributeValue(qname);
		if (val != null) {
			return val.trim();
		}
		Element property = findElementByAttr(bean.elements("property"), "name", name);
		if (property == null) {
			return null;
		}
		val = property.attributeValue("value");
		if (val != null) {
			return val.trim();
		}
		List<Element> valueList = property.elements("value");
		if (valueList.size() == 1) {
			return valueList.get(0).getTextTrim();
		}
		return null;
	}
	
	private void loadLocationInPlaceholderBean(Element bean, boolean checkSuffix) {
		String location = findPropertyValue(bean, "location");
		if (location != null && (!checkSuffix || location.endsWith(".properties"))) {
			propertiesFileLocations.add(location);
		}
	}
	
	// load location.
	private void loadPropertiesLocationFromPlaceholderConfiguer(Element root) {
		Element configuer = findElementByAttr(root.elements("bean"), 
				"class", "org.springframework.beans.factory.config.PropertyPlaceholderConfigurer");
		if (configuer == null) {
			return;
		}
		loadLocationInPlaceholderBean(configuer, false);
	}
	
	
	/**
	 * Find locations in a placeholder configuer bean. This method checks
	 * whether the locations ends with '.properties' if checkSuffix is true.
	 * @param configuer
	 * @param checkSuffix
	 * @return locations found, or an empty list else.
	 */
	private List<String> findLocationsInPlaceholderBean(Element configuer, boolean checkSuffix) {
		List<String> list = new LinkedList<>();
		Element locations = findElementByAttr(configuer.elements("property"), "name", "locations");
		if (locations == null) {
			return list;
		}
		Element value = locations.element("value");
		if (value != null) {
			String text = value.getTextTrim();
			if (!TextUtil.isEmpty(text) && 
				(!checkSuffix || text.endsWith(".properties"))) {
				propertiesFileLocations.add(value.getTextTrim());
			}
			return list;
		}
		Element collection = locations.element("list");
		if (collection == null) {
			collection = locations.element("set");
		}
		if (collection != null) {
			for (Element valueElement: collection.elements("value")) {
				String text = valueElement.getTextTrim();
				if (!TextUtil.isEmpty(text) &&
					(!checkSuffix || text.endsWith(".properties"))) {
					list.add(text);
				}
			}
		}
		return list;
	}
	
	// load locations.
	private void loadPropertiesLocationsFromPlaceholderConfiguer(Element root) {
		Element configuer = findElementByAttr(root.elements("bean"), 
				"class", "org.springframework.beans.factory.config.PropertyPlaceholderConfigurer");
		if (configuer == null) {
			return;
		}
		propertiesFileLocations.addAll(findLocationsInPlaceholderBean(configuer, false));
	}
	
	private void loadPropertiesLocations() {
		Element root = document.getRootElement();
		loadPropertiesLocationFromPlaceholderConfiguer(root);
		loadPropertiesLocationsFromPlaceholderConfiguer(root);
		if (!propertiesFileLocations.isEmpty()) {
			return;
		}
		//Reaching here means no standard Spring placeholder bean was found, then we guess one.
		for (Element bean : root.elements("bean")) {
			if (hasProperty(bean, "location")) {
				loadLocationInPlaceholderBean(bean, true);
			}
			Element locations = findElementByAttr(bean.elements("property"), "name", "locations");
			if (locations == null) {
				continue;
			}
			propertiesFileLocations.addAll(findLocationsInPlaceholderBean(bean, true));
			if (!propertiesFileLocations.isEmpty()) {
				break;
			}
		}
	}
	
	private boolean hasProperty(Element parent, String attrName) {
		QName qname = new QName(attrName, new Namespace("p", P_NAMESPACE));
		if (parent.attributeValue(qname) != null) {
			return true;
		}
		return findElementByAttr(parent.elements("property"), "name", attrName) != null;
	}
	
	private boolean isDatasourceBean(Element element) {
		String clazz = element.attributeValue("class");
		if (null == clazz) {
			return false;
		}
		if (DATASOURCE_CLASSES[0].equals(clazz)
				|| DATASOURCE_CLASSES[1].equals(clazz)) {
			return true;
		}
		String id = element.attributeValue("id");
		if ((id == null || !id.toLowerCase().contains("datasource")) &&
			!clazz.toLowerCase().contains("datasource")) {
			return false;
		}
		if (hasProperty(element, "url") &&
			hasProperty(element, "username") &&
			hasProperty(element, "password")) {
			return true;
		}
		return false;
	}

	private String parsePropertyValue(Element datasourceBean, String propertyName) {
		QName qname = new QName(propertyName, new Namespace("p", P_NAMESPACE));
		if (null != datasourceBean.attributeValue(qname)) {
			return datasourceBean.attributeValue(qname).trim();
		}
		for (Element property : datasourceBean.elements("property")) {
			String nameAttr = property.attributeValue("name");
			if (nameAttr == null || !nameAttr.equals(propertyName)) {
				continue;
			}
			String value = property.attributeValue("value");
			return value == null ? value : value.trim();
		}
		return null;
	}

	private String propertyValueFromDatasources(String propertyName) {
		Element root = document.getRootElement();
		List<Element> beanElements = root.elements("bean");
		for (Element beanElement : beanElements) {
			if (!isDatasourceBean(beanElement)) {
				continue;
			}
			String username = parsePropertyValue(beanElement, propertyName);
			if (username != null) {
				return username;
			}
		}
		return null;
	}

	/**
	 * Get the username from the datasource bean. if there are multiple
	 * datasource beans, the first one will be used.
	 * 
	 * @return the username if there is one, null otherise.
	 */
	public String getDbUsername() {
		return propertyValueFromDatasources("username");
	}

	/**
	 * Get the driver class' name from the datasource bean. if there are
	 * multiple datasource beans, the first one will be used.
	 * 
	 * @return the driver class' name if there is one, null otherwise.
	 */
	public String getDbDriverClass() {
		return propertyValueFromDatasources("driverClassName");
	}

	/**
	 * Get the password from the datasource bean. if there are multiple
	 * datasource beans, the first one will be used.
	 * 
	 * @return the password if there is one, null otherwise.
	 */
	public String getDbPassword() {
		return propertyValueFromDatasources("password");
	}

	/**
	 * Get the url from the datasource bean. if there are multiple
	 * datasource beans, the first one will be used.
	 * 
	 * @return the url if there is one, null otherwise.
	 */
	public String getDbUrl() {
		return propertyValueFromDatasources("url");
	}
	
	/**
	 * Get the imported spring file locations if any.
	 * @return locations if any, an empty set else.
	 */
	public Set<String> getImportedLocations() {
		return this.importLocations;
	}
	
	/**
	 * Get properties file locations configured in PropertyPlaceholderConfigurer bean.
	 * @return locations if any, an empty set else.
	 */
	public Set<String> getPropertiesFileLocations() {
		return propertiesFileLocations;
	}

}
