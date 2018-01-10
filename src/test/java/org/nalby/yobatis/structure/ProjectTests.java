package org.nalby.yobatis.structure;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class ProjectTests {


	private class TestedProject extends Project {
		@Override
		protected String findMavenRepositoryPath() {
			return "maven";
		}
		public TestedProject (Folder folder) {
			root = folder;
		}
	}

	private Folder mockedFolder;
	private TestedProject project;
	
	@Before
	public void manvePath() {
		mockedFolder = mock(Folder.class);
		when(mockedFolder.path()).thenReturn("/test");
		when(mockedFolder.name()).thenReturn("test");
		project = new TestedProject(mockedFolder);
	}
	
	
	@Test
	public void contcat() {
		assertTrue(project.concatMavenRepositoryPath("/test").equals("maven/test"));
	}
	
	
	@Test
	public void pathAndName() {
		assertTrue(project.name() == "test");
		assertTrue(project.path() == "/test");
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void lists() {
		when(mockedFolder.listFiles()).thenReturn(Collections.EMPTY_LIST);
		when(mockedFolder.listFolders()).thenReturn(Collections.EMPTY_LIST);
		assertTrue(project.listFiles().isEmpty());
		assertTrue(project.listFolders().isEmpty());
	}
	
	@Test
	public void find() {
		when(mockedFolder.findFolder("folder2")).thenReturn(mock(Folder.class));
		assertTrue(project.findFolder("folder2") != null);
		verify(mockedFolder, times(1)).findFolder("folder2");

		assertTrue(project.findFolder("/test/folder1") == null);
		verify(mockedFolder, times(1)).findFolder("folder1");

		assertTrue(project.findFile("folder1") == null);
	}
	

	@Test
	public void create() {
		when(mockedFolder.createFile("file")).thenReturn(mock(File.class));
		when(mockedFolder.createFolder("folder")).thenReturn(mock(Folder.class));
		project.createFile("file");
		project.createFolder("folder");
		verify(mockedFolder, times(1)).createFile("file");
		verify(mockedFolder, times(1)).createFolder("folder");
	}
	
	
}
