package org.nalby.yobatis.structure;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class FolderTests {
	
	private static class TestFolder extends AbstractFolder {

		public TestFolder(String path, String name) {
			this.name = name;
			this.path = path;
		} 

		@Override
		protected List<FolderV1> doListFolders() {
			return folders;
		}
		
		public void setFolders(List<FolderV1> list) {
			folders = list;
		}
		
		public void addFolder(FolderV1 folder) {
			folders.add(folder);
		}
		
		@Override
		protected List<File> doListFiles() {
			return files;
		}
		
		public void setFiles(List<File> list) {
			files = list;
		}
	}
	
	private TestFolder testFolder;
	
	private List<FolderV1> folders;

	private List<File> files;
	
	private static class FolderData {
		public List<FolderV1> subfolders;
		public List<File> files;
	}
	
	private Map<FolderV1, FolderData> folderDataMap;

	@Before
	public void setup() {
		testFolder = new TestFolder("/test", "test");
		folders = new LinkedList<>();
		files = new LinkedList<>();
		testFolder.setFolders(folders);
		testFolder.setFiles(files);

		folderDataMap = new HashMap<>();

		FolderData folderData = new FolderData();
		folderData.subfolders = folders;
		folderData.files = files;
		folderDataMap.put(testFolder, folderData);
	}
	
	public File mockFile(String path, String name) {
		File file = mock(File.class);
		when(file.name()).thenReturn(name);
		when(file.path()).thenReturn(path);
		return file;
	}
	
	private FolderV1 mockFolder(String path, String name) {
		FolderV1 folder = mock(FolderV1.class);
		when(folder.path()).thenReturn(path);
		when(folder.name()).thenReturn(name);

		FolderData folderData = new FolderData();
		folderData.files = new LinkedList<>();
		folderData.subfolders = new LinkedList<>();
		when(folder.listFiles()).thenReturn(folderData.files);
		when(folder.listFolders()).thenReturn(folderData.subfolders);
		folderDataMap.put(folder, folderData);
		return folder;
	}
	
	public void addFileToFolder(FolderV1 folder, String ... names) {
		FolderData folderData = folderDataMap.get(folder);
		for (String name: names) {
			folderData.files.add(mockFile(folder.path(), name));
		}
	}
	
	public void addFolderToFolder(FolderV1 dst, FolderV1 src) {
		FolderData folderData = folderDataMap.get(dst);
		folderData.subfolders.add(src);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void findWithInvalidPath() {
		try {
			testFolder.findFolder(null);
			fail();
		} catch (IllegalArgumentException e) {

		}

		try {
			testFolder.findFolder("");
			fail();
		} catch (IllegalArgumentException e) {

		}
		testFolder.findFolder("/test/");
	}
	
	
	@Test
	public void findWithoubfolders() {
		testFolder.setFolders(null);
		assertTrue(testFolder.findFolder("test") == null);

		testFolder.setFolders(folders);
		assertTrue(testFolder.findFolder("test") == null);
	}
	
	
	@Test
	public void findDepthOne() {
		FolderV1 tmp = new TestFolder("/test/depth1", "depth1");
		testFolder.addFolder(tmp);
		assertTrue(testFolder.findFolder("depth1") == tmp);
		assertTrue(tmp.path().equals("/test/depth1"));
		assertTrue(tmp.name().equals("depth1"));
	}
	
	@Test
	public void findDepthTwo() {
		TestFolder deepth1 = new TestFolder("/test/depth1", "depth1");

		FolderV1 deepth2 = new TestFolder("/test/depth1/depth2", "depth2");
		List<FolderV1> tmp = new LinkedList<>();
		tmp.add(deepth2);
		deepth1.setFolders(tmp);
		deepth1.addFolder(deepth2);

		testFolder.addFolder(deepth1);

		assertTrue(testFolder.findFolder("depth1/depth2") == deepth2);
		assertTrue(deepth2.path().equals("/test/depth1/depth2"));
		assertTrue(deepth2.name().equals("depth2"));
	}
	
	@Test
	public void listWithoutFiles() {
		assertTrue(testFolder.listFiles().isEmpty());
		testFolder.setFiles(null);
		assertTrue(testFolder.listFiles().isEmpty());
	}
	
	
	@Test
	public void findNullFile() {
		File file = testFolder.findFile("file1");
		assertNull(file);

		file = testFolder.findFile("depth1/file2");
		assertNull(file);

		addFileToFolder(testFolder, "file2");
		file = testFolder.findFile("file1");
		assertNull(file);
	}

	
	@Test
	public void findDepth1File() {
		addFileToFolder(testFolder, "file1");
		File file = testFolder.findFile("file1");
		assertTrue(file.name().equals("file1"));
	}
	
	@Test
	public void findDepth2File() {
		FolderV1 depth1 = mockFolder(testFolder.path() + "/depth1", "depth1");
		addFolderToFolder(testFolder, depth1);
		addFileToFolder(depth1, "file1");
		File file = testFolder.findFile("depth1/file1");
		assertTrue(file.name().equals("file1"));
	}

}
