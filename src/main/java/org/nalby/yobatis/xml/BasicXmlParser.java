package org.nalby.yobatis.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.nalby.yobatis.util.Expect;

public abstract class BasicXmlParser {
	
	private static final int MAX_STREAM_SIZE = 5 * 1024 * 1024;

	Document document;
	
	public BasicXmlParser(InputStream inputStream, String rootElmentTag) throws DocumentException, IOException {
		Expect.asTrue(inputStream != null
				&& inputStream.available() <= MAX_STREAM_SIZE
				&& inputStream.available() > 0,
				"invalid input stream");
		Expect.notNull(rootElmentTag, "root element tag is null.");
		SAXReader saxReader = new SAXReader();
		saxReader.setValidation(false);
		document = saxReader.read(new BufferedInputStream(inputStream));
		if (document == null || document.getRootElement() == null
				|| !rootElmentTag.equals(document.getRootElement().getName())) {
			throw new DocumentException("Unpexpected document.");
		}
	}

}
