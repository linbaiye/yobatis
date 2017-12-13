package org.nalby.yobatis;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.nalby.yobatis.exception.ProjectNotFoundException;
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;
import org.nalby.yobatis.mybatis.MybatisConfigReader;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.sql.mysql.Mysql.Builder;
import org.nalby.yobatis.structure.LogFactory;
import org.nalby.yobatis.structure.Logger;
import org.nalby.yobatis.structure.PomParser;
import org.nalby.yobatis.structure.PropertiesParser;
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
	
	
	private String searchProperty(PropertiesParser propertiesParser, 
			PomParser pomParser, String property) {
		if (!PropertiesParser.isPlaceholder(property)) {
			return property;
		}
		String tmp = propertiesParser.getProperty(property);
		return PropertiesParser.isPlaceholder(tmp) ? pomParser.getProfileProperty(tmp) : tmp;
	}
	
	/**
	 *  Build the generator of mybatis-generator's config file according to project config.
	 */
	private MybatisConfigFileGenerator buildMybatisGeneratorConfigMaker(Project project) {
		WebXmlParser webXmlParser = WebXmlParser.build(project);
		PomParser pomParser = new PomParser(project);
		SpringParser springParser = new SpringParser(project,
				webXmlParser.getSpringConfigLocations());
		PropertiesParser propertiesParser = 
				new PropertiesParser(project, springParser.getPropertiesFilePaths());
		
		String username = searchProperty(propertiesParser, pomParser,
				springParser.getDatabaseUsername());

		String password = searchProperty(propertiesParser, pomParser,
				springParser.getDatabasePassword());

		String url = searchProperty(propertiesParser, pomParser,
				springParser.getDatabaseUrl());

		String driverClassName = searchProperty(propertiesParser, pomParser,
				springParser.getDatabaseDriverClassName());

		String dbJarPath = pomParser.getDatabaseJarPath(driverClassName);

		Builder builder = Mysql.builder();
		builder.setConnectorJarPath(dbJarPath)
		.setDriverClassName(driverClassName)
		.setUsername(username)
		.setPassword(password)
		.setUrl(url);
		return new MybatisConfigFileGenerator(project, builder.build());
	}
	
	
	/*
	 * Merge the new file generator into the existent one if exists.
	 */
	private MybatisConfigReader mergeIntoExistentConfig(MybatisConfigFileGenerator configFileGenerator, Project project) {
		MybatisConfigReader reader = configFileGenerator;
		InputStream inputStream = null;
		try {
			inputStream = project.getInputStream(reader.getConfigeFilename());
			MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(inputStream);
			mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(configFileGenerator);
			reader = mybatisXmlParser;
		} catch (Exception e) {
			logger.info("No existent configuration found, will generate a new one.");
		} finally {
			project.closeInputStream(inputStream);
		}
		return reader;
	}


	private void fun2() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			IProject project = workspace.getRoot().getProject("diaowen");
			if (!project.exists()) {
				throw new ProjectNotFoundException();
			}
			EclipseProject eclipseProject = new EclipseProject(project);

			MybatisConfigFileGenerator configFileGenerator = buildMybatisGeneratorConfigMaker(eclipseProject);

			MybatisConfigReader reader = mergeIntoExistentConfig(configFileGenerator, eclipseProject);

			//Write mybatis-generator's config file to the project's root dir.
			eclipseProject.writeFile(reader.getConfigeFilename(), reader.asXmlText());

			MybatisFilesWriter filesWriter = new MybatisFilesWriter(eclipseProject, reader);
			filesWriter.writeAll();
			logger.info("Generated files.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Caught exception:{}.", e.getMessage());
		}
	}
	
	private void fun3() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject("diaowen");
		if (!project.exists()) {
				throw new ProjectNotFoundException();
		}
		EclipseProject eclipseProject = new EclipseProject(project);
		WebXmlParser webXmlParser = WebXmlParser.build(eclipseProject);
		SpringParser springParser  = new SpringParser(eclipseProject, webXmlParser.getSpringConfigLocations());
		PropertiesParser propertiesParser = new PropertiesParser(eclipseProject, springParser.getPropertiesFilePaths());
		System.out.println(springParser.getDatabaseUsername());
		System.out.println(propertiesParser.getProperty(springParser.getDatabaseUsername()));
		System.out.println(springParser.getDatabasePassword());
		System.out.println(springParser.getDatabaseUrl());
		System.out.println(springParser.getDatabaseDriverClassName());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		fun2();
		return null;
	}
}
