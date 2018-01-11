package org.nalby.yobatis.structure;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TestUtil;

public class SpringAntPathFileManagerTests {
	
	private SpringAntPathFileManager fileManager;
	
	private PomTree pomTree;
	
	private Pom webpom;
	
	/**
	 * The webapp folder of webpm.
	 */
	private Folder webappFolder;

	/**
	 * Resource folders of webpom.
	 */
	private Set<Folder> resourceFolders;
	
	private Project project;
	
	private Set<Pom> poms;
	
	/**
	 * used to add files to a folder.
	 */
	private Map<Folder, List<File>> filesOfFolders;
	
	
	private Pom mockPom() {
		Pom pom = mock(Pom.class);
		poms.add(pom);
		return pom;
	}
	
	
	@Before
	public void setup() {
		pomTree = mock(PomTree.class);
		poms = new HashSet<>();
		when(pomTree.getPoms()).thenReturn(poms);
		
		/* Setup webpom. */
		webpom = mockPom();
		when(webpom.filterPlaceholders(anyString())).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return (String)invocation.getArguments()[0];
			}
		});
		webappFolder = TestUtil.mockFolder("/yobatis/src/main/webapp");
		when(webpom.getWebappFolder()).thenReturn(webappFolder);

		resourceFolders = new HashSet<>();
		when(webpom.getResourceFolders()).thenReturn(resourceFolders);

		when(pomTree.getWarPom()).thenReturn(webpom);
		
		project = mock(Project.class);

		fileManager = new SpringAntPathFileManager(pomTree, project);
		
		filesOfFolders = new HashMap<>();
	}
	
	private void addFileToFolder(Folder folder, String ... names) {
		List<File> files = filesOfFolders.get(folder);
		if (files == null) {
			files = new LinkedList<>();
			filesOfFolders.put(folder, files);
			when(folder.listFiles()).thenReturn(files);
		}
		for (String name: names) {
			File file = mock(File.class);
			when(file.parentFolder()).thenReturn(folder);
			String path = FolderUtil.concatPath(folder.path(), name);
			when(file.path()).thenReturn(path);
			files.add(file);
		}
	}

	@Test
	public void emptyDir() {
		assertTrue(fileManager.findSpringFiles("/test.xml").isEmpty());
	}
	
	@Test
	public void nullHint() {
		assertTrue(fileManager.findSpringFiles("").isEmpty());
	}
	
	@Test
	public void relativePath() {
		addFileToFolder(webappFolder, "conf.xml", "test.xml");
		Set<File> files = fileManager.findSpringFiles("test.xml");
		assertTrue(files.size() == 1);
		TestUtil.assertCollectionContains(filesOfFolders.get(webappFolder), files);
	}
	
}
