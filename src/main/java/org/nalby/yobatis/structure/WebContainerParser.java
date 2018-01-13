package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.xml.WebXmlParser;

/**
 * Search for init param-values defined in web.xml, both in application context and 
 * Servlet context.
 * @author Kyle Lin
 *
 */
public final class WebContainerParser {
	
	private File file;
	
	private Set<String> springInitParamValues;
	
	public WebContainerParser(Pom webpom) {
		Expect.notNull(webpom, "webappFolder must not be null.");
		Folder webappFolder = webpom.getWebappFolder();
		if (webappFolder == null) {
			throw new UnsupportedProjectException("Failed to find webapp dir.");
		}
		file = webappFolder.findFile("WEB-INF/web.xml");
		if (file == null) {
			throw new UnsupportedProjectException("Failed to find web.xml.");
		}
	}
	
	/**
	 * Search init param values in web.xml, which imply the locations of spring's 
	 * configuration files.
	 * 
	 * @return the values, or an empty set (immutable) if not found.
	 */
	@SuppressWarnings("unchecked")
	public Set<String> searchInitParamValues() {
		if (springInitParamValues != null) {
			return springInitParamValues;
		}
		try (InputStream inputStream = file.open()) {
			WebXmlParser parser = new WebXmlParser(inputStream);
			springInitParamValues = parser.getSpringInitParamValues();
		} catch (Exception e) {
			//Do nothing.
		}
		if (springInitParamValues == null) {
			springInitParamValues = Collections.EMPTY_SET;
		}
		return springInitParamValues;
	}

}
