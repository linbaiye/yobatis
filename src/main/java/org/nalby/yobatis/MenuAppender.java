package org.nalby.yobatis;

import java.io.FileInputStream;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.nalby.yobatis.xml.PomXmlParser;

public class MenuAppender extends ContributionItem {

	public MenuAppender() {
	}

	public MenuAppender(String id) {
		super(id);
	}
	
	private boolean isClickedOnRootProject(IProject project) {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(project.getLocationURI().getPath() + "/pom.xml");
			return new PomXmlParser(inputStream).isRootPom();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {}
		}
	}

	@Override
	public void fill(Menu menu, int index) {
		ISelectionService selectionService = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (element == null) {
			return;
		}
		if (!(element instanceof IProject) && !(element instanceof IFile)) {
			return;
		}
		if (element instanceof IProject && !isClickedOnRootProject((IProject)element)) {
			return;
		}
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
		menuItem.setText("Yobatis");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = commandService.getCommand("org.nalby.yobatis.command.generation");
				IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
				try {
					command.executeWithChecks(executionEvent);
				} catch (Exception e1) {
					//e1.printStackTrace();
				}
			}
		});
	}

}
