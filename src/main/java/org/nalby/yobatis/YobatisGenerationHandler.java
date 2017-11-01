package org.nalby.yobatis;


import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mybatis.generator.api.GeneratedJavaFile;
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
			Project project1 = EclipseProject.build("learn");
			Sql sql = new Mysql(project1);
			MybatisFilesGenerator generator = new MybatisFilesGenerator(project1, sql, new LibraryRunner());
			generator.writeAllFiles();
			generator.writeJavaFiles();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("learn");
			if (project.exists() && !project.isOpen()) {
				project.open(null);
			}
			/*String home = Platform.getUserLocation().getURL().getPath();
			WebXmlParser webXmlParser = new WebXmlParser(new FileInputStream(project.getLocationURI().getPath() + "/.m2/repository/" + "/src/main/webapp/WEB-INF/web.xml"));
			Set<String> configs = webXmlParser.getServletConfigLocation();
			String tmp = null;
			for (String k: configs) {
				tmp = k;
				break;
			}*/
				if (project.exists() && !project.isOpen()) {
					project.open(null);
				}
				//project.
			IWorkbenchWindow window = HandlerUtil
					.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openInformation(window.getShell(), "Yobatis", "Done");
			
			//project.wirteGeneratorConfigFile(path, source);
			//project.wirteGeneratorConfigFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
