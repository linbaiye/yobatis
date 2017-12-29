package org.nalby.yobatis.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FolderTests {
	
	
	@Test
	public void filename() {
		assertTrue(FolderUtil.filename("test").equals("test"));
		assertTrue(FolderUtil.folderPath("test").equals(""));
	}

	@Test
	public void relativepath() {
		assertTrue(FolderUtil.filename("hello/test").equals("test"));
		assertTrue(FolderUtil.folderPath("hello/test").equals("hello"));
	}

	@Test
	public void absolutePath() {
		assertTrue(FolderUtil.filename("/hello/test").equals("test"));
		assertTrue(FolderUtil.filename("/test").equals("test"));
		assertTrue(FolderUtil.folderPath("/hello/test").equals("/hello"));
	}
	

}
