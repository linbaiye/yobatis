package org.nalby.yobatis;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mybatis.generator.api.LibraryRunner;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.structure.MybatisFilesGenerator;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.eclipse.EclipseProject;

public class YobatisGenerationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
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

}
