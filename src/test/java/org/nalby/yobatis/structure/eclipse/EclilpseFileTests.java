package org.nalby.yobatis.structure.eclipse;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.structure.Folder;

public class EclilpseFileTests {
	
	private IFile mockedIFile;
	
	private File file;
	
	private Folder mockedParent;
	
	@Before
	public void setup() {
		mockedIFile = mock(IFile.class);
		mockedParent = mock(Folder.class);
		when(mockedParent.path()).thenReturn("/test");
		when(mockedIFile.getName()).thenReturn("file");
		file = new EclipseFile(mockedParent, mockedIFile);
	}

	@Test
	public void pathAndName() {
		assertTrue(file.path().equals("/test/file"));
		assertTrue(file.name().equals("file"));
		assertTrue(file.parentFolder() == mockedParent);
	}
	
	@Test(expected = ResourceNotAvailableExeception.class)
	public void writeException() throws CoreException {
		when(mockedIFile.exists()).thenReturn(false);
		InputStream inputStream = mock(InputStream.class);
		doThrow(new IllegalArgumentException("error"))
		.when(mockedIFile).create(inputStream, IResource.FILE, null);
		file.write(inputStream);
	}
	
	@Test
	public void writeInputStream() throws CoreException {
		when(mockedIFile.exists()).thenReturn(true);
		InputStream inputStream = mock(InputStream.class);
		doNothing().when(mockedIFile).create(inputStream, IResource.FILE, null);
		file.write(inputStream);
		verify(mockedIFile, times(1)).delete(true, null);
		verify(mockedIFile).refreshLocal(0, null);
	}
	
	@Test(expected = ResourceNotAvailableExeception.class)
	public void openException() throws CoreException {
		when(mockedIFile.getContents()).thenThrow(new IllegalArgumentException(""));
		file.open();
	}
	
	@Test
	public void open() throws CoreException {
		when(mockedIFile.getContents()).thenReturn(mock(InputStream.class));
		assertTrue(null != file.open());
	}
	
	
	@Test
	public void writeString() throws CoreException {
		when(mockedIFile.exists()).thenReturn(false);
		file.write("test");
	}
	
	@Test(expected = ResourceNotAvailableExeception.class)
	public void writeStringResourceException() throws CoreException {
		when(mockedIFile.exists()).thenReturn(true);
		doThrow(new ResourceNotAvailableExeception("e")).when(mockedIFile).delete(true, null);
		file.write("test");
	}

	@Test
	public void createFile() throws CoreException {
		File file = EclipseFile.createFile(mockedParent, mockedIFile);
		assertTrue(file.name().equals(mockedIFile.getName()));
	}
	
	@Test(expected = ResourceNotAvailableExeception.class)
	public void createFileException() throws CoreException {
		File file = EclipseFile.createFile(null, mockedIFile);
		assertTrue(file.name().equals(mockedIFile.getName()));
	}

}
