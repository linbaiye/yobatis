package org.nalby.yobatis.structure;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.util.TestUtil;

public class FolderTests {
	
	@FunctionalInterface
	private static interface CreateFolderHandler {
		Folder handle(String name);
	}

	@FunctionalInterface
	private static interface CreateFileHandler {
		File handle(String name);
	}
	
	private static class TestFolder extends AbstractFolder {

		private CreateFileHandler createFileHandler;
		
		private CreateFolderHandler createFolderHandler;

		public TestFolder(String path, String name) {
			this.name = name;
			this.path = path;
		} 

		@Override
		protected List<Folder> doListFolders() {
			return folders;
		}
		
		public void setFolders(List<Folder> list) {
			folders = list;
		}
		
		public void addFolder(Folder folder) {
			folders.add(folder);
		}
		
		@Override
		protected List<File> doListFiles() {
			return files;
		}
		
		public void setFiles(List<File> list) {
			files = list;
		}

		@Override
		protected Folder doCreateFolder(String name) {
			return createFolderHandler.handle(name);
		}

		@Override
		protected File doCreateFile(String name) {
			return createFileHandler.handle(name);
		}
	}
	
	private TestFolder testFolder;
	
	private List<Folder> folders;
	
	//Just dont want to use map.get("subfolders").
	private static class FolderData {
		public List<Folder> subfolders;
		public List<File> files;
	}
	
	private Map<Folder, FolderData> folderDataMap;
	
	private TestFolder buildTestFolder(String path, String name) {
		TestFolder folder = new TestFolder(path, name);
		FolderData folderData = new FolderData();
		folderData.subfolders = new LinkedList<>();
		folderData.files = new LinkedList<>();

		folder.setFolders(folderData.subfolders);
		folder.setFiles(folderData.files);

		folderDataMap.put(folder, folderData);
		return folder;
	}

	@Before
	public void setup() {
		folderDataMap = new HashMap<>();
		testFolder = buildTestFolder("/test", "test");
		folders = folderDataMap.get(testFolder).subfolders;
	}
	
	public File mockFile(String path, String name) {
		File file = mock(File.class);
		when(file.name()).thenReturn(name);
		when(file.path()).thenReturn(path);
		return file;
	}
	
	public void addFileToFolder(Folder folder, String ... names) {
		FolderData folderData = folderDataMap.get(folder);
		for (String name: names) {
			folderData.files.add(mockFile(folder.path(), name));
		}
	}
	
	public void addFileToFolder(Folder folder, File file) {
		FolderData folderData = folderDataMap.get(folder);
		folderData.files.add(file);
	}
	
	
	public void addFolderToFolder(Folder dst, Folder src) {
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
		Folder tmp = new TestFolder("/test/depth1", "depth1");
		testFolder.addFolder(tmp);
		assertTrue(testFolder.findFolder("depth1") == tmp);
		assertTrue(tmp.path().equals("/test/depth1"));
		assertTrue(tmp.name().equals("depth1"));
	}
	
	@Test
	public void findDepthTwo() {
		TestFolder deepth1 = new TestFolder("/test/depth1", "depth1");

		Folder deepth2 = new TestFolder("/test/depth1/depth2", "depth2");
		List<Folder> tmp = new LinkedList<>();
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
		Folder depth1 = buildTestFolder(testFolder.path() + "/depth1", "depth1");
		addFolderToFolder(testFolder, depth1);
		addFileToFolder(depth1, "file1");
		File file = testFolder.findFile("depth1/file1");
		assertTrue(file.name().equals("file1"));
	}
	

	@Test
	public void createDepth1Folder() {
		Folder folder = buildTestFolder(testFolder.path() + "/hello", "hello");
		testFolder.createFolderHandler = (String name) -> { 
			if ("hello".equals(name)) {
				return folder;
			}
			throw new ResourceNotAvailableExeception("error.");
		};
		assertTrue(testFolder.createFolder("hello") == folder);
		List<Folder> list = testFolder.listFolders();
		TestUtil.assertCollectionSizeAndContains(list, 1, folder);
	}
	
	
	@Test
	public void createDepth2Folder() {
		TestFolder depth1 = buildTestFolder(testFolder.path() + "/depth1", "depth1");
		//First depth should be hit.
		addFolderToFolder(testFolder, depth1);

		TestFolder depth2 = buildTestFolder(testFolder.path() + "/depth1/depth2", "depth2");
		depth1.createFolderHandler = (String name) -> { 
			if ("depth2".equals(name)) {
				return depth2;
			}
			throw new ResourceNotAvailableExeception("error.");
		};
		assertTrue(testFolder.createFolder("depth1/depth2") == depth2);

		assertTrue(testFolder.findFolder("depth1/depth2") == depth2);
	}
	
	//Need to create file.
	@Test
	public void createDepth1File() {
		File file = mockFile(testFolder.path() + "/file", "file");
		testFolder.createFileHandler = (String name) -> { 
			if (name.equals("file")) {
				return file;
			}
			throw new ResourceNotAvailableExeception("err");
		};
		assertTrue(testFolder.createFile("file") == file);

		//Make sure the created file was added to the list.
		TestUtil.assertCollectionSizeAndContains(testFolder.listFiles(), 1, file);
	}
	
	//When the file to create is already existed.
	@Test
	public void createDepth1FileWhenExisted() {
		File file = mockFile(testFolder.path() + "/file", "file");
		addFileToFolder(testFolder, file);
		assertTrue(testFolder.createFile("file") == file);
	}
	
	@Test
	public void createDepth2File() {
		TestFolder depth1 = buildTestFolder(testFolder.path() + "/depth1", "depth1");
		testFolder.createFolderHandler = (String name) -> {
			if ("depth1".equals(name)) {
				return depth1;
			}
			throw new ResourceNotAvailableExeception("err");
		};

		File file = mockFile(testFolder.path() + "/depth1/file", "file");
		depth1.createFileHandler = (String name) -> { 
			if (name.equals("file")) {
				return file;
			}
			throw new ResourceNotAvailableExeception("err");
		};
		assertTrue(testFolder.createFile("depth1/file") == file);
	}

}
