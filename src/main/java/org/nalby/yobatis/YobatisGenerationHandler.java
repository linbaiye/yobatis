package org.nalby.yobatis;


import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mybatis.generator.api.LibraryRunner;
import org.nalby.yobatis.exception.ProjectNotFoundException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.MybatisFilesGenerator;
import org.nalby.yobatis.structure.PomParser;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.SpringParser;
import org.nalby.yobatis.structure.Project.FolderSelector;
import org.nalby.yobatis.structure.eclipse.EclipseProject;
import org.nalby.yobatis.xml.PomXmlParser;
import org.nalby.yobatis.xml.WebXmlParser;

public class YobatisGenerationHandler extends AbstractHandler {
	
	
	private void displayMessage(ExecutionEvent event, String message) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(), "Yobatis", message);
	}
	
	
	private Object work(ExecutionEvent event) throws ExecutionException {
		try {
			ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();

			ISelection selection = selectionService.getSelection();
			if (!(selection instanceof IStructuredSelection)) {
				return null;
			}
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element == null || !(element instanceof IProject)) {
				return null;
			}
			IProject thisProject = (IProject)element;
			if (thisProject.exists() && !thisProject.isOpen()) {
				thisProject.open(null);
			}
			Project project = EclipseProject.build(thisProject.getName());
			Sql sql = new Mysql(project);
			MybatisFilesGenerator generator = new MybatisFilesGenerator(project, sql, new LibraryRunner());
			generator.writeAllFiles();
		} catch (Exception e) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openInformation(window.getShell(), "Yobatis", e.getMessage());
		}
		return null;
	}
	
	
	private String getWebXmlPath(Project project) {
		List<Folder> folders = project.findFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return folder.containsFile("web.xml");
			}
		});
		if (folders.size() != 1) {
			throw new UnsupportedProjectException("Unable to find web.xml");
		}
		Folder folder = folders.get(0);
		return project.convertToFullPath(folder.path() + "/web.xml");
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("uplending-all");
		if (!project.exists()) {
			throw new ProjectNotFoundException();
		}
		//IFolder folder = project.getFolder("/learn");
		try {
			EclipseProject eclipseProject = new EclipseProject(project);
			PomParser parser = new PomParser(eclipseProject);
			System.out.println(parser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver"));
			/*SpringParser parser  = new SpringParser(eclipseProject);
			System.out.println(parser.getDatabaseDriverClassName());
			System.out.println(parser.getDatabaseUrl());
			System.out.println(parser.getDatabasePassword());
			System.out.println(parser.getDatabaseUsername());
			System.out.println(parser.getPropertiesFilePath());*/
			//String webxmlPath = getWebXmlPath(eclipseProject);
			//WebXmlParser parser = new WebXmlParser(new FileInputStream(new File(webxmlPath)));
			//List<String> springConfigPaths = parser.getSpringConfigLocations();
			/*for (String path: springConfigPaths) {
				System.out.println(path);
			}*/

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
