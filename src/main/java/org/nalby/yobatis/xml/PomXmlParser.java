package org.nalby.yobatis.xml;

import java.io.IOException;
import java.io.InputStream;

import org.dom4j.DocumentException;
import org.dom4j.Element;

public class PomXmlParser extends BasicXmlParser {

	public PomXmlParser(InputStream inputStream)
			throws DocumentException, IOException {
		super(inputStream, "project");
	}
	
	public boolean isRootPom() {
		Element parent = document.getRootElement().element("parent");
		return parent == null;
	}

}
