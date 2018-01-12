package org.nalby.yobatis.structure.eclipse;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.nalby.yobatis.exception.UnsupportedProjectException;

public class EclipseProjectTests {
	
	private IProject mockedProject;
	
	
	@Before
	public void setup() {
		mockedProject = mock(IProject.class);
		when(mockedProject.isOpen()).thenReturn(true);
		when(mockedProject.getName()).thenReturn("name");
	}
	
	
	@Test(expected = UnsupportedProjectException.class)
	public void whenError() throws CoreException {
		when(mockedProject.isOpen()).thenReturn(false);
		doThrow(new IllegalArgumentException("error")).when(mockedProject).open(null);
		new EclipseProject(mockedProject);
	}
	

}
