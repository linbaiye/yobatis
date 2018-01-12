package org.nalby.yobatis.structure;

import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Set;

public class WebContainerParserTests {
	
	private File mockedFile;
	
	private Pom mockedPom;
	
	private Folder mockedFolder;

	@Before
	public void setup() {
		mockedFile = mock(File.class);
		mockedPom = mock(Pom.class);
		mockedFolder = mock(Folder.class);
		when(mockedPom.getWebappFolder()).thenReturn(mockedFolder);
		when(mockedFolder.findFile("WEB-INF/web.xml")).thenReturn(mockedFile);
	}
	

	@Test(expected = UnsupportedProjectException.class)
	public void nullWebfolder() {
		when(mockedPom.getWebappFolder()).thenReturn(null);
		new WebContainerParser(mockedPom);
	}

	@Test(expected = UnsupportedProjectException.class)
	public void nullFile() {
		when(mockedFolder.findFile("WEB-INF/web.xml")).thenReturn(null);
		new WebContainerParser(mockedPom);
	}
	
	@Test
	public void invalidXml() {
		when(mockedFile.open()).thenThrow(new ResourceNotAvailableExeception("not valid"));
		WebContainerParser webContainerParser = new WebContainerParser(mockedPom);
		Set<String> ret = webContainerParser.searchInitParamValues();
		assertTrue(ret.isEmpty());
		assertTrue(webContainerParser.searchInitParamValues() == ret);
	}
	
	@Test
	public void validXml() {
		String xml = "<web-app>"
				+ "<context-param><param-name>contextConfigLocation</param-name><param-value>test</param-value></context-param>"
				+ "</web-app>";
		when(mockedFile.open()).thenReturn(new ByteArrayInputStream(xml.getBytes()));
		WebContainerParser webContainerParser = new WebContainerParser(mockedPom);
		Set<String> ret = webContainerParser.searchInitParamValues();
		assertTrue(ret.size() == 1);
	}


}
