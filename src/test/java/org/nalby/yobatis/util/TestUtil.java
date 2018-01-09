package org.nalby.yobatis.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.nalby.yobatis.structure.Folder;

import static org.junit.Assert.assertTrue;

public class TestUtil {

	public static void assertStringsInCollection(Collection<String> collection, String ... list)  {
		for (String tmp: list) {
			assertTrue(collection.contains(tmp));
		}
	}
	
	/**
	 * Assert that the {@code collection} is of size {@code collectionSize}, and that
	 * the strings are all contained by the collection.
	 * @param collection
	 * @param collectionSize
	 * @param list the strings.
	 */
	public static void assertCollectionSizeAndStringsIn(Collection<String> collection,
			int collectionSize,
			String ... list)  {
		assertTrue(collectionSize == collection.size());
		for (String tmp: list) {
			assertTrue(collection.contains(tmp));
		}
	}
	
	public static void dumpStringCollection(Collection<String> collection) {
		for (String tmp: collection) {
			System.out.println(tmp);
		}
	}
	
	@SafeVarargs
	public static <T> Set<T> buildSet(T ... args) {
		Set<T> set = new HashSet<T>();
		for (T t : args) {
			set.add(t);
		}
		return set;
	}
	
	public static Folder mockFolder(String path) {
		Folder folder = mock(Folder.class);
		when(folder.path()).thenReturn(path);
		String name = FolderUtil.filename(path);
		when(folder.name()).thenReturn(name);
		return folder;
	}

}
