/*
 *    Copyright 2018 the original author or authors.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *    use this file except in compliance with the License.  You may obtain a copy
 *    of the License at
 *    
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *    License for the specific language governing permissions and limitations under
 *    the License.
 */
package org.nalby.yobatis.structure.eclipse;

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
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.mybatis.MybatisGenerator;

public class MenuAppender extends ContributionItem {
	
	static {
		LogFactory.setLogger(EclipseLogger.class);
	}

	public MenuAppender() {
	}

	public MenuAppender(String id) {
		super(id);
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
		if (element == null ||
			(!(element instanceof IProject) && !(element instanceof IFile))) {
			return;
		}
		if (element instanceof IFile) {
			IFile iFile = (IFile)element;
			if (!MybatisGenerator.CONFIG_FILENAME.equals(iFile.getName())) {
				return;
			}
		}
		MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
		menuItem.setText("Yobatis");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ICommandService commandService = (ICommandService) PlatformUI
						.getWorkbench().getService(ICommandService.class);
				Command command = commandService
						.getCommand("org.nalby.yobatis.command.generation");
				IHandlerService handlerService = (IHandlerService) PlatformUI
						.getWorkbench().getService(IHandlerService.class);
				ExecutionEvent executionEvent = handlerService
						.createExecutionEvent(command, new Event());
				try {
					command.executeWithChecks(executionEvent);
				} catch (Exception e1) {

				}
			}
		});
	}

}
