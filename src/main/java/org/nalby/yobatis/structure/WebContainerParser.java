package org.nalby.yobatis.structure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.DocumentException;
import org.nalby.yobatis.exception.ResourceNotFoundException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.xml.WebXmlParser;

public class WebContainerParser {
	
	private WebXmlParser parser;
	
	private Set<String> springInitParamValues;
	
	public WebContainerParser(Project project, Folder webappFolder) {
		Expect.notNull(project, "project must not be empty.");
		Expect.notNull(webappFolder, "webappFolder must not be null.");
		InputStream inputStream = null;
		try {
			inputStream = project.getInputStream(webappFolder.path() + "/WEB-INF/web.xml");
			springInitParamValues = new HashSet<String>();
			parser = new WebXmlParser(inputStream);
			Set<String> values = parser.getSpringInitParamValues();
			springInitParamValues.addAll(values);
		} catch (FileNotFoundException e) {
			throw new ResourceNotFoundException(e);
		} catch (IOException e) {
			throw new UnsupportedProjectException(e);
		} catch (DocumentException e) {
			throw new UnsupportedProjectException(e);
		} finally {
			project.closeInputStream(inputStream);
		}
	}
	
	public Set<String> getSpringInitParamValues() {
		return springInitParamValues;
	}

}
