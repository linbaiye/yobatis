package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.nalby.yobatis.exception.ResourceNotFoundException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.xml.WebXmlParser;

public class WebContainerParser {
	
	private WebXmlParser parser;
	
	private Set<String> springInitParamValues;
	
	public WebContainerParser(Pom webpom) {
		Expect.notNull(webpom, "webappFolder must not be null.");
		InputStream inputStream = null;
		try {
			inputStream = webpom.getWebappFolder().openFile("WEB-INF/web.xml");
			springInitParamValues = new HashSet<String>();
			parser = new WebXmlParser(inputStream);
			Set<String> values = parser.getSpringInitParamValues();
			springInitParamValues.addAll(values);
		} catch (Exception e) {
			throw new ResourceNotFoundException(e);
		} finally {
			FolderUtil.closeStream(inputStream);
		}
	}
	
	public Set<String> getSpringInitParamValues() {
		return springInitParamValues;
	}

}
