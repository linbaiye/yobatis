package org.nalby.yobatis.structure.eclipse;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.ResourceNotAvailableExeception;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.structure.Folder;

public class EclipseFolderTests {
	
	private EclipseFolder eclipseFolder;

	private IProject mockedProject;
	
	private IFolder mockedFolder;
	
	private interface ResourceCreator {
		IResource create(String name);
	}
	
	private IResource[] appendResources(ResourceCreator creator, 
			IResource[] origin, String ...names) {
		IResource[] resources = null;
		int originSize = 0;
		if (origin == null) {
			resources = new IResource[names.length];
		} else {
			originSize = origin.length;
			resources = new IResource[origin.length + names.length];
			System.arraycopy(origin, 0, resources, 0, origin.length);
		}
		for (int i = 0; i < names.length; i++) {
			IResource resource = creator.create(names[i]);
			resources[i + originSize] = resource;
		}
		return resources;
	}

	
	private IResource[] appendFiles(IResource[] origin, String ... names) {
		return appendResources((String name) -> {
			IFile iFile = mock(IFile.class);
			when(iFile.getType()).thenReturn(IResource.FILE);
			when(iFile.getName()).thenReturn(name);
			return  iFile;
		},  origin, names);
	}
	
	private IResource[] appendFolders(IResource[] origin, String ... names) {
		return appendResources((String name) -> {
			IFolder iFile = mock(IFolder.class);
			when(iFile.getType()).thenReturn(IResource.FOLDER);
			when(iFile.getName()).thenReturn(name);
			return  iFile;
		},  origin, names);
	}
	
	@Before
	public void setup() {
		mockedProject = mock(IProject.class);
		when(mockedProject.getName()).thenReturn("project");
		mockedFolder = mock(IFolder.class);

		when(mockedFolder.getName()).thenReturn("folder");
		eclipseFolder = new EclipseFolder("/", (IResource)mockedProject);
	}
	
	@Test
	public void listEmptyFolders() throws CoreException {
		when(mockedProject.members()).thenReturn(new IResource[0]);
		assertTrue(eclipseFolder.doListFolders().isEmpty());
	}
	
	@Test(expected = ResourceNotAvailableExeception.class)
	public void listFoldersWhenException() throws CoreException {
		when(mockedProject.members()).thenThrow(new IllegalArgumentException());
		eclipseFolder.doListFolders();
	}
	
	@Test
	public void listFolders() throws CoreException {
		IResource[] resources = appendFiles(null, "file1", "file2");
		resources = appendFolders(resources, "folder1", "folder2");
		when(mockedProject.members()).thenReturn(resources);
		List<Folder> folders = eclipseFolder.doListFolders();
		assertTrue(folders.size() == 2);
		for (Folder folder : folders) {
			assertTrue(folder.name().equals("folder1") || folder.name().equals("folder2"));
		}
	}
	
	
	@Test
	public void listFiles() throws CoreException {
		IResource[] resources = appendFiles(null, "file1");
		resources = appendFolders(resources, "folder1", "folder2");
		when(mockedProject.members()).thenReturn(resources);
		List<File> files = eclipseFolder.doListFiles();
		assertTrue(files.size() == 1);
		assertTrue(files.get(0).name().equals("file1"));
	}
	
	@Test
	public void doCreateFile() {
		IFile iFile = mock(IFile.class);
		when(iFile.getName()).thenReturn("file");
		when(mockedProject.getFile("file")).thenReturn(iFile);
		assertTrue(eclipseFolder.doCreateFile("file").name().equals("file"));
	}
	
	
	@Test(expected = ResourceNotAvailableExeception.class)
	public void doCreateFileWhenException() {
		when(mockedProject.getFile(anyString())).thenThrow(new IllegalArgumentException("error."));
		eclipseFolder.doCreateFile("test");
	}
	

	@Test
	public void doCreateFolder() {
		IFolder iFolder = mock(IFolder.class);
		when(iFolder.getName()).thenReturn("folder");
		when(mockedProject.getFolder("folder")).thenReturn(iFolder);
		assertTrue(eclipseFolder.doCreateFolder("folder").name().equals("folder"));
	}
	

	@Test(expected = ResourceNotAvailableExeception.class)
	public void doCreateFolderWhenException() {
		when(mockedProject.getFolder(anyString())).thenThrow(new IllegalArgumentException("error."));
		eclipseFolder.doCreateFolder("test");
	}
	
	@Test
	public void resourceIsFolder() throws CoreException {
		when(mockedFolder.members()).thenReturn(new IResource[0]);
		eclipseFolder = new EclipseFolder("/", mockedFolder);
		assertTrue(eclipseFolder.listFiles().isEmpty());

		IFolder iFolder = mock(IFolder.class);
		when(iFolder.getName()).thenReturn("folder1");
		when(mockedFolder.getFolder("folder1")).thenReturn(iFolder);
		assertTrue(eclipseFolder.doCreateFolder("folder1").name().equals("folder1"));

		IFile iFile = mock(IFile.class);
		when(iFile.getName()).thenReturn("file");
		when(mockedFolder.getFile("file")).thenReturn(iFile);
		assertTrue(eclipseFolder.doCreateFile("file").name().equals("file"));
	}
	

}
