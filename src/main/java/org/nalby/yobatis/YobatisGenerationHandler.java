package org.nalby.yobatis;


import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.sql.mysql.Mysql.Builder;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.MybatisConfigFileGenerator;
import org.nalby.yobatis.structure.MybatisFilesGenerator;
import org.nalby.yobatis.structure.PomParser;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.PropertiesParser;
import org.nalby.yobatis.structure.SpringParser;
import org.nalby.yobatis.structure.Project.FolderSelector;
import org.nalby.yobatis.structure.eclipse.EclipseProject;
import org.nalby.yobatis.xml.PomXmlParser;
import org.nalby.yobatis.xml.WebXmlParser;

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
			Project project = null; //EclipseProject.build(thisProject.getName());
			Sql sql = null;//new Mysql(project);
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
			WebXmlParser webXmlParser = WebXmlParser.build(eclipseProject);
			PomParser pomParser = new PomParser(eclipseProject);
			SpringParser springParser  = new SpringParser(eclipseProject, webXmlParser.getSpringConfigLocations());
			PropertiesParser propertiesParser = new PropertiesParser(eclipseProject, pomParser, springParser.getPropertiesFilePath());
			Builder builder = Mysql.builder();
			builder.setConnectorJarPath(eclipseProject.concatMavenResitoryPath(pomParser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver")))
			.setDriverClassName(propertiesParser.getProperty(springParser.getDatabaseDriverClassName()))
			.setUsername(propertiesParser.getProperty(springParser.getDatabaseUsername()))
			.setPassword(propertiesParser.getProperty(springParser.getDatabasePassword()))
			.setUrl(propertiesParser.getProperty(springParser.getDatabaseUrl()));
			Sql mysql = builder.build();
			MybatisConfigFileGenerator configFile = new MybatisConfigFileGenerator(eclipseProject, mysql);
			System.out.println(configFile.getXmlConfig());
			eclipseProject.writeFile(MybatisConfigFileGenerator.CONFIG_FILENAME,  configFile.getXmlConfig());
			//System.out.println(parser.getPropertiesFilePath());*/
			//String webxmlPath = getWebXmlPath(eclipseProject);
			//WebXmlParser parser = new WebXmlParser(new FileInputStream(new File(webxmlPath)));
			//List<String> springConfigPaths = parser.getSpringConfigLocations();
			/*for (String path: springConfigPaths) {
				System.out.println(path);
			}*/

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
