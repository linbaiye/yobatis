package org.nalby.yobatis.util;

import static org.junit.Assert.*;

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
	
	@Test
	public void contact() {
		assertTrue(FolderUtil.concatPath("/", "hello").equals("/hello"));
		assertTrue(FolderUtil.concatPath("/", "/hello").equals("/hello"));
		assertTrue(FolderUtil.concatPath("/", "/hello").equals("/hello"));
		assertTrue(FolderUtil.concatPath("/wolrd/", "/hello").equals("/wolrd/hello"));
	}
	
	@Test
	public void extractPackageName() {
		assertNull(FolderUtil.extractPackageName(""));
		assertNull(FolderUtil.extractPackageName(null));
		assertNull(FolderUtil.extractPackageName("/src/main/java/"));
		assertNull(FolderUtil.extractPackageName("/src/main/java"));
		assertTrue("model".equals(FolderUtil.extractPackageName("/src/main/java/model")));
		assertTrue("test.model".equals(FolderUtil.extractPackageName("/src/main/java/test/model")));
	}
	
	
	@Test
	public void eliminatePackagePath() {
		assertNull(FolderUtil.wipePackagePath(null));
		assertTrue("".equals(FolderUtil.wipePackagePath("")));
		assertTrue("/src/main/java".equals(FolderUtil.wipePackagePath("/src/main/java")));
		assertTrue("/src/main/java".equals(FolderUtil.wipePackagePath("/src/main/java/model")));
		assertTrue("/src/main/java/".equals(FolderUtil.wipePackagePath("/src/main/java/")));
	}
	

}
