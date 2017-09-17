package org.nalby.yobatis;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.RootFolder;
import org.nalby.yobatis.structure.eclipse.EclipseFolder;

public class YobatisGenerationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("test");
		try {
			if (project.exists() && !project.isOpen()) {
				project.open(null);
			}
			IFolder ifolder = project.getFolder("src/main/java");
			Folder folder = new EclipseFolder(null, ifolder);
			RootFolder rootFolder = new RootFolder(folder);
			String daoPath = rootFolder.daoFolderPath();
			IWorkbenchWindow window = HandlerUtil
					.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openInformation(window.getShell(), "Yobatis",
					daoPath);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}
