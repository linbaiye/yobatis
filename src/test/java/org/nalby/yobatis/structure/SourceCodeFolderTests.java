package org.nalby.yobatis.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.runners.MockitoJUnitRunner;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.SourceCodeFolder;

@RunWith(MockitoJUnitRunner.class)
public class SourceCodeFolderTests {
	
	@Mock
	private Folder mockedFolder;
	
	private List<Folder> buildMockedSubFolders(int nr, FolderCustomer customer) {
		List<Folder> result = new LinkedList<Folder>();
		for (int i = 0; i < nr; i++) {
			Folder mockedFolder = mock(Folder.class);
			customer.customFolder(i, mockedFolder);
			result.add(mockedFolder);
		}
		return result;
	}
	
	private interface FolderCustomer {
		public void customFolder(int nr, Folder mockedFolder);
	}
	
	@Test
	public void testNullLayer() {
		when(mockedFolder.folders()).thenReturn(new ArrayList<Folder>());
		SourceCodeFolder rootFolder = new SourceCodeFolder(mockedFolder);
		assertTrue(null == rootFolder.modelFolderPath());
		assertTrue(null == rootFolder.daoFolderPath());
	}

	@Test
	public void testDaoLayer() {
		List<Folder> list = buildMockedSubFolders(2, new FolderCustomer() {
			@Override
			public void customFolder(int nr, Folder mockedFolder) {
				when(mockedFolder.containsFolders()).thenReturn(false);
				if (0 == nr) {
					when(mockedFolder.isDaoLayer()).thenReturn(false);
				} else {
					when(mockedFolder.isDaoLayer()).thenReturn(true);
					when(mockedFolder.path()).thenReturn("com.org.dao");
				}
			}
		});
		when(mockedFolder.folders()).thenReturn(list);
		SourceCodeFolder rootFolder = new SourceCodeFolder(mockedFolder);
		assertTrue("com.org.dao".equals(rootFolder.daoFolderPath()));
	}

}
