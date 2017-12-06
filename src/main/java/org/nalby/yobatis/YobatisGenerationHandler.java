package org.nalby.yobatis;


import java.io.IOException;
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
import org.eclipse.ui.handlers.HandlerUtil;
import org.nalby.yobatis.exception.ProjectNotFoundException;
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;
import org.nalby.yobatis.mybatis.MybatisConfigReader;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.sql.Sql;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.sql.mysql.Mysql.Builder;
import org.nalby.yobatis.structure.LogFactory;
import org.nalby.yobatis.structure.Logger;
import org.nalby.yobatis.structure.PomParser;
import org.nalby.yobatis.structure.DatabasePropertiesParser;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.SpringParser;
import org.nalby.yobatis.structure.eclipse.EclipseLogger;
import org.nalby.yobatis.structure.eclipse.EclipseProject;
import org.nalby.yobatis.xml.MybatisXmlParser;
import org.nalby.yobatis.xml.WebXmlParser;

public class YobatisGenerationHandler extends AbstractHandler {
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	static {
		LogFactory.setLogger(EclipseLogger.class);
	}

	private void displayMessage(ExecutionEvent event, String message) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(), "Yobatis", message);
	}
	
	/*
	 * Build a sql connector based on project's configurations.
	 */
	private Sql buildSqlConnector(Project project) {
		WebXmlParser webXmlParser = WebXmlParser.build(project);
		PomParser pomParser = new PomParser(project);
		SpringParser springParser  = new SpringParser(project, webXmlParser.getSpringConfigLocations());
		DatabasePropertiesParser propertiesParser = new DatabasePropertiesParser(project, pomParser, springParser);

		Builder builder = Mysql.builder();
		builder.setConnectorJarPath(project.concatMavenResitoryPath(pomParser.dbConnectorJarRelativePath("com.mysql.jdbc.Driver")))
		.setDriverClassName(propertiesParser.getDatabaseDriverClassName())
		.setUsername(propertiesParser.getDatabaseUsername())
		.setPassword(propertiesParser.getDatabasePassword())
		.setUrl(propertiesParser.getDatabaseUrl());
		return builder.build();
	}
	
	
	private MybatisConfigFileGenerator buildMybatisGeneratorConfigMaker(Project project) {
		Sql mysql = buildSqlConnector(project);
		return new MybatisConfigFileGenerator(project, mysql);
	}

	private void fun2() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		//IProject project = workspace.getRoot().getProject("uplending-all");
		try {
			IProject project = workspace.getRoot().getProject("learn");
			if (!project.exists()) {
				throw new ProjectNotFoundException();
			}
			EclipseProject eclipseProject = new EclipseProject(project);
			MybatisConfigFileGenerator configFileGenerator = buildMybatisGeneratorConfigMaker(eclipseProject);
			MybatisConfigReader reader = configFileGenerator;
			MybatisXmlParser mybatisXmlParser = null;
			try {
				mybatisXmlParser = new MybatisXmlParser(eclipseProject.getInputStream(MybatisConfigFileGenerator.CONFIG_FILENAME));
			}  catch (Exception e) {
			}
			String xmlFileContent = null;
			if (mybatisXmlParser != null) {
				xmlFileContent = mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(configFileGenerator);
				reader = mybatisXmlParser;
			} else {
				xmlFileContent = configFileGenerator.getXmlConfig();
			}
			eclipseProject.writeFile(MybatisConfigFileGenerator.CONFIG_FILENAME, xmlFileContent);
			MybatisFilesWriter filesWriter = new MybatisFilesWriter(eclipseProject, reader);
			filesWriter.writeAll();
			logger.info("Generated files.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		fun2();
		return null;
	}

}
