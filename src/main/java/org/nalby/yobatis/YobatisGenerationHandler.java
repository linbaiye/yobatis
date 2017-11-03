package org.nalby.yobatis;


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
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.MybatisFilesGenerator;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.Project.FolderSelector;
import org.nalby.yobatis.structure.eclipse.EclipseProject;

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
			List<Folder> folders = eclipseProject.findFolders(new FolderSelector() {
				@Override
				public boolean isSelected(Folder folder) {
					return folder.containsFile("web.xml");
				}
			});
			for (Folder folder: folders) {
				System.out.println(folder.name());
				System.out.println(folder.path());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
