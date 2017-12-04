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
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.LibraryRunner;
import org.nalby.yobatis.exception.ProjectNotFoundException;
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;
import org.nalby.yobatis.mybatis.MybatisConfigReader;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.sql.mysql.Mysql.Builder;
import org.nalby.yobatis.structure.PomParser;
import org.nalby.yobatis.structure.DatabasePropertiesParser;
import org.nalby.yobatis.structure.SpringParser;
import org.nalby.yobatis.structure.eclipse.EclipseProject;
import org.nalby.yobatis.xml.MybatisXmlParser;
import org.nalby.yobatis.xml.WebXmlParser;

public class YobatisGenerationHandler extends AbstractHandler {
	
	
	private void displayMessage(ExecutionEvent event, String message) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(), "Yobatis", message);
	}
	
	private void fun2() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		//IProject project = workspace.getRoot().getProject("uplending-all");
		IProject project = workspace.getRoot().getProject("learn");
		if (!project.exists()) {
			throw new ProjectNotFoundException();
		}
		//IFolder folder = project.getFolder("/learn");
		try {
			EclipseProject eclipseProject = new EclipseProject(project);
			WebXmlParser webXmlParser = WebXmlParser.build(eclipseProject);
			PomParser pomParser = new PomParser(eclipseProject);
			SpringParser springParser  = new SpringParser(eclipseProject, webXmlParser.getSpringConfigLocations());
			DatabasePropertiesParser propertiesParser = new DatabasePropertiesParser(eclipseProject, pomParser, springParser);
			Builder builder = Mysql.builder();
			builder.setConnectorJarPath(eclipseProject.concatMavenResitoryPath(pomParser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver")))
			.setDriverClassName(propertiesParser.getDatabaseDriverClassName())
			.setUsername(propertiesParser.getDatabaseUsername())
			.setPassword(propertiesParser.getDatabasePassword())
			.setUrl(propertiesParser.getDatabaseUrl());
			Sql mysql = builder.build();
			MybatisConfigFileGenerator generator = new MybatisConfigFileGenerator(eclipseProject, mysql);
			MybatisConfigReader reader = generator;
			System.out.println(generator.getXmlConfig());
			MybatisXmlParser mybatisXmlParser = null;
			try {
				mybatisXmlParser = new MybatisXmlParser(eclipseProject.getInputStream(MybatisConfigFileGenerator.CONFIG_FILENAME));
			}  catch (Exception e) {
			}
			String xmlFileContent = null;
			if (mybatisXmlParser != null) {
				xmlFileContent = mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(generator);
				reader = mybatisXmlParser;
			} else {
				xmlFileContent = generator.getXmlConfig();
			}
			eclipseProject.writeFile(MybatisConfigFileGenerator.CONFIG_FILENAME, xmlFileContent);
			MybatisFilesWriter filesWriter = new MybatisFilesWriter(eclipseProject, reader);
			filesWriter.writeAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("uplending-all");
		if (!project.exists()) {
			throw new ProjectNotFoundException();
		}
		// IFolder folder = project.getFolder("/learn");
		EclipseProject eclipseProject = new EclipseProject(project);
		System.out.println(eclipseProject.getFullPath());*/
		fun2();
		return null;
	}

}
