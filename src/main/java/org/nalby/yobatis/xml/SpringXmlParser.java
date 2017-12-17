package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
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
		loadPropertieLocationsInPropertHolder();
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
	
	private void loadPropertieLocationsInPropertHolder() {
		Element root = document.getRootElement();
		List<Element> elements = root.elements("bean");
		for (Element bean: elements) {
			List<Element> properties = bean.elements("property");
			for (Element property: properties) {
				String name = property.attributeValue("name");
				if (name == null || !"locations".equals(name.trim())) {
					continue;
				}
				Element listElement = property.element("list");
				if (listElement == null) {
					break;
				}
				List<Element> valueElements = listElement.elements("value");
				for (Element valueElement: valueElements) {
					if (!TextUtil.isEmpty(valueElement.getText())) {
						propertiesFileLocations.add(valueElement.getTextTrim());
					}
				}
			}
		}
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
	 * @return the imported files.
	 */
	public Set<String> getImportedLocations() {
		return this.importLocations;
	}
	
	/**
	 * Get properties file paths.
	 * @return file paths starting with 'classpath'.
	 */
	public Set<String> getPropertiesFileLocations() {
		return propertiesFileLocations;
	}

}
