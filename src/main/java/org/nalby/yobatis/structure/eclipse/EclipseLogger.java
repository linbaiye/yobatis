package org.nalby.yobatis.structure.eclipse;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.nalby.yobatis.log.Logger;

public class EclipseLogger implements Logger {
	
	private String className;
	
	public EclipseLogger(String className) {
		this.className = className;
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();

		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
	
	
	private void openConsole(IConsole myConsole) throws PartInitException {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view = (IConsoleView) page.showView(id);
		view.display(myConsole);
	}

	@Override
	public void info(String format, Object... args) {
		if (format == null) {
			return;
		}
		String fmt = format.replaceAll("\\{\\}", "%s");
		String result = String.format(fmt, args);
		try {
			MessageConsole myConsole = findConsole("yobatis");
			openConsole(myConsole);
			MessageConsoleStream out = myConsole.newMessageStream();
			out.write("[ " + className + ".java ] " + result + "\n");
		} catch (Exception e) {
			//Ignore.
		}
	}

}
