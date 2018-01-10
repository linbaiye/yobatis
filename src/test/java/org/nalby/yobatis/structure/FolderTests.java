package org.nalby.yobatis.structure;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

	@Before
	public void setup() {
		testFolder = new TestFolder("/test", "test");
		folders = new LinkedList<>();
		files = new LinkedList<>();
		testFolder.setFolders(folders);
		testFolder.setFiles(files);
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

}
