package org.nalby.yobatis.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.Project.FolderSelector;

@RunWith(MockitoJUnitRunner.class)
public class ProjectTests {
	
	private class TestingProject extends Project {
		public TestingProject(Folder root) {
			this.root = root;
		} 

		@Override
		public String getDatabaseUrl() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDatabaseUsername() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDatabasePassword() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDatabaseDriverClassName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDatabaseConnectorPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDatabaseConnectorFullPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getSourceCodeDirPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getResourcesDirPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getModelLayerPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDaoLayerPath() {
			// TODO Auto-generated method stub
			return null;
		}


		@Override
		public void createDir(String dirPath) {
			// TODO Auto-generated method stub
		}

		@Override
		public String concatMavenResitoryPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	private List<Folder> buildMockedSubFolders(int nr ) {
		List<Folder> result = new LinkedList<Folder>();
		for (int i = 0; i < nr; i++) {
			result.add(mock(Folder.class));
		}
		return result;
	}
	
	@Test
	public void testFindFoldersWithEmptySub() {
		Folder mockedRoot = mock(Folder.class);
		when(mockedRoot.getSubFolders()).thenReturn(new ArrayList<Folder>());
		Project project = new TestingProject(mockedRoot);
		List<Folder> result = project.findFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return true;
			}
		});
		assertTrue(result.isEmpty());
	}

	@Test
	public void testFindFolders() {
		List<Folder> list = buildMockedSubFolders(10);
		Folder mockedRoot = mock(Folder.class);
		when(mockedRoot.getSubFolders()).thenReturn(list);
		Project project = new TestingProject(mockedRoot);
		List<Folder> result = project.findFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return true;
			}
		});
		assertTrue(result.size() == 10);
		result = project.findFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return false;
			}
		});
		assertTrue(result.isEmpty());
	}

	
	@Test
	public void testWriteFileOnlyFileName() {
		Folder mockedRoot = mock(Folder.class);
		Project project = new TestingProject(mockedRoot);
		project.writeFile("test.con", "hello");
		verify(mockedRoot).writeFile("test.con", "hello");
	}
	
	@Test
	public void testWriteFileWithRootPath() {
		Folder mockedRoot = mock(Folder.class);
		Project project = new TestingProject(mockedRoot);
		when(mockedRoot.path()).thenReturn("/test");
		project.writeFile("/test/test.con", "hello");
		verify(mockedRoot).writeFile("test.con", "hello");
	}
	
	@Test
	public void testWriteFileWithAbspath() {
		Folder mockedRoot = mock(Folder.class);
		Project project = new TestingProject(mockedRoot);
		when(mockedRoot.path()).thenReturn("/test");
		when(mockedRoot.createFolder("axxxx")).thenReturn(mockedRoot);
		project.writeFile("/test/axxxx/test.con", "hello");
		verify(mockedRoot).createFolder("axxxx");
		verify(mockedRoot).writeFile("test.con", "hello");
	}
	@Test
	public void testWriteFileWithRelativepath() {
		Folder mockedRoot = mock(Folder.class);
		Project project = new TestingProject(mockedRoot);
		when(mockedRoot.createFolder("axxxx")).thenReturn(mockedRoot);
		project.writeFile("axxxx/test.con", "hello");
		verify(mockedRoot).createFolder("axxxx");
		verify(mockedRoot).writeFile("test.con", "hello");
	}
}
