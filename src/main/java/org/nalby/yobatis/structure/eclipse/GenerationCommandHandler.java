package org.nalby.yobatis.structure.eclipse;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.nalby.yobatis.Yobatis;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.mybatis.MybatisGenerator;

public class GenerationCommandHandler extends AbstractHandler {
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	private void start() {
		ISelectionService selectionService = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (element == null ||
			(!(element instanceof IProject) && !(element instanceof IFile))) {
			return;
		}
		if (element instanceof IFile) {
			IFile iFile = (IFile)element;
			if (MybatisGenerator.CONFIG_FILENAME.equals(iFile.getName())) {
				IProject project = iFile.getProject();
				Yobatis.generate(new EclipseProject(project));
			}
		} else {
			Yobatis.generate(new EclipseProject((IProject)element));
		}
	}


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			start();
		} catch (Exception e) {
			logger.error("Caught exception:{}.", e);
		}
		return null;
	}
}
