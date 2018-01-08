package org.nalby.yobatis.structure.eclipse;


import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.log.AbstractLogger;

public class EclipseLogger extends AbstractLogger implements IConsoleFactory {
	
	private final static String VIEW_NAME = "Yobatis";
	
	private MessageConsole yobatisConsole;
	
	public EclipseLogger(String className) {
		super(className);
	}

	@Override
	public void openConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (VIEW_NAME.equals(existing[i].getName())) {
				yobatisConsole = (MessageConsole) existing[i];
				return;
			}
		}
		// no console found, so create a new one
		try {
			yobatisConsole = new MessageConsole(VIEW_NAME, null);
			conMan.addConsoles(new IConsole[] { yobatisConsole });
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = win.getActivePage();
			String id = IConsoleConstants.ID_CONSOLE_VIEW;
			IConsoleView view = (IConsoleView) page.showView(id);
			view.display(yobatisConsole);
		} catch (Exception e) {
			throw new ProjectException(e);
		}
	}

	@Override
	protected void wirteToConsole(String msg) {
		if (msg == null) {
			return;
		}
		try {
			openConsole();
			try (MessageConsoleStream out = yobatisConsole.newMessageStream()) {
				out.write(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
