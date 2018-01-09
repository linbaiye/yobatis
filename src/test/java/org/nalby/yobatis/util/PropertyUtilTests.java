package org.nalby.yobatis.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class PropertyUtilTests {
	
	@Test
	public void isPlaceholder() {
		assertFalse(PropertyUtil.isPlaceholder("test"));

		assertFalse(PropertyUtil.isPlaceholder("${test"));

		assertFalse(PropertyUtil.isPlaceholder("${}}"));

		assertFalse(PropertyUtil.isPlaceholder("${{}"));

		assertTrue(PropertyUtil.isPlaceholder("${}"));

		assertTrue(PropertyUtil.isPlaceholder("${t}"));

		assertTrue(PropertyUtil.isPlaceholder(" ${t}   "));

		assertTrue(PropertyUtil.isPlaceholder(" ${t   }   "));
	}
	
	@Test
	public void valueOfPlaceholder() {
		assertTrue("t".equals(PropertyUtil.valueOfPlaceholder(" ${t   }   ")));
		assertTrue("t t".equals(PropertyUtil.valueOfPlaceholder(" ${  t t   }   ")));
		assertTrue("".equals(PropertyUtil.valueOfPlaceholder(" ${   }   ")));
		assertTrue("".equals(PropertyUtil.valueOfPlaceholder(" ${}")));
	}
	

	@Test
	public void containsPlaceholder() {
		assertFalse(PropertyUtil.containsPlaceholder(null));
		assertFalse(PropertyUtil.containsPlaceholder(""));
		assertFalse(PropertyUtil.containsPlaceholder("  "));
		assertFalse(PropertyUtil.containsPlaceholder("${"));
		assertTrue(PropertyUtil.containsPlaceholder("${}"));
		assertTrue(PropertyUtil.containsPlaceholder("x${ xx } "));
		assertTrue(PropertyUtil.containsPlaceholder("x${ x x } "));
	}
	
	private void assertStringsInList(List<String> set, String ... list) {
		for (String str: list) {
			assertTrue(set.contains(str));
		}
	}
	
	@Test
	public void placeholdersFrom() {
		List<String> tmp = PropertyUtil.placeholdersFrom(null);
		assertTrue(tmp.isEmpty());

		tmp = PropertyUtil.placeholdersFrom("xx");
		assertTrue(tmp.isEmpty());

		tmp = PropertyUtil.placeholdersFrom("${}");
		assertStringsInList(tmp, "${}");

		tmp = PropertyUtil.placeholdersFrom("${} ${xxx} ${22}");
		assertStringsInList(tmp, "${}", "${xxx}", "${22}");

		tmp = PropertyUtil.placeholdersFrom("xx${xx} ${xx}");
		assertTrue(tmp.size() == 2);
		assertStringsInList(tmp, "${xx}", "${xx}");
	}
}
