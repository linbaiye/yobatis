package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.Project.FolderSelector;

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
			if (paramValue == null || "".equals(paramValue.trim())) {
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
				if (value == null || "".equals(value.trim())) {
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
	 * @throws UnsupportedProjectException if this project is out of scope, for instance, no spring's configuration or more
	 * that 2 configuration files found.
	 */
	public List<String> getSpringConfigLocations() {
		List<String> result = new LinkedList<String>();
		String tmp = getAppConfigLocation();
		if (tmp != null) {
			result.add(tmp);
		}
		tmp = getServletConfigLocation();
		if (tmp != null) {
			result.add(tmp);
		}
		if (result.isEmpty()) {
			throw new UnsupportedProjectException("Unable to find spring's config files.");
		}
		return result;
	}
	
	
	public static WebXmlParser build(Project project) {
		List<Folder> folders = project.findFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return  folder.path().contains("src/main/webapp/WEB-INF") && folder.containsFile("web.xml");
			}
		});
		if (folders.size() != 1) {
			throw new UnsupportedProjectException("Unable to find web.xml");
		}
		Folder folder = folders.get(0);
		InputStream inputStream = null;
		try {
			inputStream = project.getInputStream(folder.path() + "/web.xml");
			return new WebXmlParser(inputStream);
		} catch (IOException e) {
			throw new UnsupportedProjectException(e);
		} catch (DocumentException e) {
			throw new UnsupportedProjectException(e);
		} finally {
			project.closeInputStream(inputStream);
		}
	}
	
}
