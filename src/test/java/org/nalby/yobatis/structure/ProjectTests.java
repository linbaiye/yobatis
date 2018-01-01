package org.nalby.yobatis.structure;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;
import org.nalby.yobatis.structure.Folder;

@RunWith(MockitoJUnitRunner.class)
public class ProjectTests {
	
	private class TestingProject extends Project {
		public TestingProject(Folder root) {
			this.root = root;
		} 

		@Override
		public String concatMavenResitoryPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	@Test
	public void writeFileDirectly() {
		Folder mockedRoot = mock(Folder.class);
		when(mockedRoot.path()).thenReturn("/test");
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
}
