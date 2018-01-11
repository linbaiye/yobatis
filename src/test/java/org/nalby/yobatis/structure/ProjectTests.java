package org.nalby.yobatis.structure;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.util.TestUtil;

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
	
	@Test
	@SuppressWarnings("unchecked")
	public void listEmptyFolders() {
		when(mockedFolder.listFolders()).thenReturn(Collections.EMPTY_LIST);
		assertTrue(Project.listAllFolders(mockedFolder).isEmpty());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void listAllFoldersOfDepth2() {
		Folder depth1 = mock(Folder.class);
		when(mockedFolder.listFolders()).thenReturn(Arrays.asList(depth1));
		//List<>
		Folder depth2 = mock(Folder.class);
		when(depth1.listFolders()).thenReturn(Arrays.asList(depth2));

		when(depth2.listFolders()).thenReturn(Collections.EMPTY_LIST);

		Set<Folder> ret = Project.listAllFolders(mockedFolder);
		TestUtil.assertCollectionSizeAndContains(ret, 2, depth1, depth2);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void listAllFils() {
		Folder depth1 = mock(Folder.class);
		File file = mock(File.class);
		when(mockedFolder.listFiles()).thenReturn(Arrays.asList(file));
		when(mockedFolder.listFolders()).thenReturn(Arrays.asList(depth1));

		File file2 = mock(File.class);
		when(depth1.listFolders()).thenReturn(Collections.EMPTY_LIST);
		when(depth1.listFiles()).thenReturn(Arrays.asList(file2));

		Set<File> ret = Project.listAllFiles(mockedFolder);
		TestUtil.assertCollectionSizeAndContains(ret, 2, file, file2);
	}
	
}
