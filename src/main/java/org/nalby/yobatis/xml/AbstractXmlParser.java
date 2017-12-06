package org.nalby.yobatis.xml;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.nalby.yobatis.util.Expect;

public abstract class AbstractXmlParser {
	
	private static final int MAX_STREAM_SIZE = 5 * 1024 * 1024;

	Document document;

	public AbstractXmlParser(InputStream inputStream, String rootElmentTag) throws DocumentException, IOException {
		Expect.asTrue(inputStream != null
				&& inputStream.available() <= MAX_STREAM_SIZE
				&& inputStream.available() > 0,
				"invalid input stream");
		Expect.notNull(rootElmentTag, "root element tag is null.");
		SAXReader saxReader = new SAXReader();
		saxReader.setValidation(false);
		customSAXReader(saxReader);
	
		document = saxReader.read(new BufferedInputStream(inputStream));
		if (document == null || document.getRootElement() == null
				|| !rootElmentTag.equals(document.getRootElement().getName())) {
			throw new DocumentException("Unpexpected document.");
		}
	}
	
	void customSAXReader(SAXReader saxReader) {}
	
	public static String toXmlString(Document document) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos, true, "utf-8");
	    XMLWriter writer = new XMLWriter(ps, format);
	    writer.write(document);
		String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		ps.close();
		return content;
	}
	
	public String toXmlString() throws IOException {
		return AbstractXmlParser.toXmlString(document);
	}
}
