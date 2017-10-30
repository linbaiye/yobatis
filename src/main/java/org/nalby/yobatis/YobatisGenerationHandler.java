package org.nalby.yobatis;


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
import org.nalby.yobatis.exception.ProjectNotFoundException;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.structure.MybatisGeneratorConfigGenerator;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.eclipse.EclipseProject;

public class YobatisGenerationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
		/*IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("learn");
			if (project.exists() && !project.isOpen()) {
				project.open(null);
			}
			String home = Platform.getUserLocation().getURL().getPath();
			WebXmlParser webXmlParser = new WebXmlParser(new FileInputStream(project.getLocationURI().getPath() + "/.m2/repository/" + "/src/main/webapp/WEB-INF/web.xml"));
			Set<String> configs = webXmlParser.getServletConfigLocation();
			String tmp = null;
			for (String k: configs) {
				tmp = k;
				break;
			}*/
			/*IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject project = workspace.getRoot().getProject("learn");
			if (!project.exists()) {
				throw new ProjectNotFoundException();
			}
				if (project.exists() && !project.isOpen()) {
					project.open(null);
				}
				IFolder ifolder = project.getFolder("src/main/java");
				//project.
			IWorkbenchWindow window = HandlerUtil
					.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openInformation(window.getShell(), "Yobatis", project1.getDaoLayerPath());*/
			Project project1 = EclipseProject.build("learn");
			Sql sql = new Mysql(project1);
			MybatisGeneratorConfigGenerator generator = new MybatisGeneratorConfigGenerator(project1, sql);
			generator.generate();
			//project.wirteGeneratorConfigFile(path, source);
			//project.wirteGeneratorConfigFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
