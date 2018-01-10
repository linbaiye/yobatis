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
		FolderV1 handle(String name);
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

		@Override
		protected FolderV1 doCreateFolder(String name) {
			return createFolderHandler.handle(name);
		}

		@Override
		protected File doCreateFile(String name) {
			return createFileHandler.handle(name);
		}
	}
	
	private TestFolder testFolder;
	
	private List<FolderV1> folders;
	
	private static class FolderData {
		public List<FolderV1> subfolders;
		public List<File> files;
	}
	
	private Map<FolderV1, FolderData> folderDataMap;
	
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
	
	public void addFileToFolder(FolderV1 folder, String ... names) {
		FolderData folderData = folderDataMap.get(folder);
		for (String name: names) {
			folderData.files.add(mockFile(folder.path(), name));
		}
	}
	
	public void addFileToFolder(FolderV1 folder, File file) {
		FolderData folderData = folderDataMap.get(folder);
		folderData.files.add(file);
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
		FolderV1 depth1 = buildTestFolder(testFolder.path() + "/depth1", "depth1");
		addFolderToFolder(testFolder, depth1);
		addFileToFolder(depth1, "file1");
		File file = testFolder.findFile("depth1/file1");
		assertTrue(file.name().equals("file1"));
	}
	

	@Test
	public void createDepth1Folder() {
		FolderV1 folder = buildTestFolder(testFolder.path() + "/hello", "hello");
		testFolder.createFolderHandler = (String name) -> { 
			if ("hello".equals(name)) {
				return folder;
			}
			throw new ResourceNotAvailableExeception("error.");
		};
		assertTrue(testFolder.createFolder("hello") == folder);
		List<FolderV1> list = testFolder.listFolders();
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
