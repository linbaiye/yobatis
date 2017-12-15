package org.nalby.yobatis.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	
	public static Set<String> buildStringSet(String ... strings) {
		Set<String> set = new HashSet<String>();
		for (String string: strings) {
			set.add(string);
		}
		return set;
	}

}
