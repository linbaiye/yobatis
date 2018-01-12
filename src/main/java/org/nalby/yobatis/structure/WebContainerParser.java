package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.xml.WebXmlParser;

public class WebContainerParser {
	
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
	
	public Set<String> getSpringInitParamValues() {
		if (springInitParamValues != null) {
			return springInitParamValues;
		}
		try (InputStream inputStream = file.open()) {
			springInitParamValues = new HashSet<String>();
			WebXmlParser parser = new WebXmlParser(inputStream);
			Set<String> values = parser.getSpringInitParamValues();
			springInitParamValues.addAll(values);
			return springInitParamValues;
		} catch (Exception e) {
			throw new ResourceNotAvailableExeception(e);
		}
	}

}
