package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.nalby.yobatis.exception.SqlConfigIncompleteException;

public class SpringXmlParser extends BasicXmlParser {

	private static final String BEANS_TAG = "beans";

	private static final String[] DATASOURCE_CLASSES = {
			"com.alibaba.druid.pool.DruidDataSource",
			"org.apache.commons.dbcp.BasicDataSource" };

	private static final String P_NAMESPACE = "http://www.springframework.org/schema/p";

	public SpringXmlParser(InputStream inputStream) throws DocumentException, IOException {
		super(inputStream, BEANS_TAG);
	}

	private boolean isDatasourceBean(Element element) {
		String clazz = element.attributeValue("class");
		if (null == clazz) {
			return false;
		}
		return DATASOURCE_CLASSES[0].equals(clazz)
				|| DATASOURCE_CLASSES[1].equals(clazz);
	}

	private String parsePropertyValue(Element datasourceBean, String propertyName) {
		QName qname = new QName(propertyName, new Namespace("p", P_NAMESPACE));
		if (null != datasourceBean.attributeValue(qname)) {
			return datasourceBean.attributeValue(qname);
		}
		for (Element property : datasourceBean.elements("property")) {
			String nameAttr = property.attributeValue("name");
			if (nameAttr == null || !nameAttr.equals(propertyName)) {
				continue;
			}
			return property.attributeValue("value");
		}
		return null;
	}

	private String propertyValueFromDatasources(String propertyName) {
		Element root = document.getRootElement();
		List<Element> beanElements = root.elements("bean");
		String username = null;
		for (Element beanElement : beanElements) {
			if (!isDatasourceBean(beanElement)) {
				continue;
			}
			username = parsePropertyValue(beanElement, propertyName);
			if (username != null) {
				return username;
			}
		}
		return username;
	}

	/**
	 * Get the username from the datasource bean. if there are multiple
	 * datasource beans, the first one will be used.
	 * 
	 * @return the username if there is one.
	 * @throws SqlConfigIncompleteException if failed to find the property.
	 */
	public String getDbUsername() {
		return propertyValueFromDatasources("username");
	}

	/**
	 * Get the driver class' name from the datasource bean. if there are
	 * multiple datasource beans, the first one will be used.
	 * 
	 * @return the driver class' name if there is one.
	 * @throws SqlConfigIncompleteException if failed to find the property.
	 */
	public String getDbDriverClass() {
		return propertyValueFromDatasources("driverClassName");
	}

	/**
	 * Get the password from the datasource bean. if there are multiple
	 * datasource beans, the first one will be used.
	 * 
	 * @return the password if there is one.
	 * @throws SqlConfigIncompleteException if failed to find the property.
	 */
	public String getDbPassword() {
		return propertyValueFromDatasources("password");
	}

	/**
	 * Get the url from the datasource bean. if there are multiple
	 * datasource beans, the first one will be used.
	 * 
	 * @return the url if there is one.
	 * @throws SqlConfigIncompleteException if failed to find the property.
	 */
	public String getDbUrl() {
		return propertyValueFromDatasources("url");
	}


}
