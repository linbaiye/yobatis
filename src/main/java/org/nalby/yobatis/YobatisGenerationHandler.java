package org.nalby.yobatis;

import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;
import org.nalby.yobatis.mybatis.MybatisConfigReader;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.sql.mysql.Mysql.Builder;
import org.nalby.yobatis.structure.LogFactory;
import org.nalby.yobatis.structure.Logger;
import org.nalby.yobatis.structure.PomParser;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.SpringParser;
import org.nalby.yobatis.structure.WebContainerParser;
import org.nalby.yobatis.structure.eclipse.EclipseProject;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.xml.MybatisXmlParser;

public class YobatisGenerationHandler extends AbstractHandler {
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	private String searchPomPropertyIfNecessary(PomParser pomParser, String property) {
		if (!PropertyUtil.isPlaceholder(property)) {
			return property;
		}
		String tmp = pomParser.getProperty(property);
		return PropertyUtil.isPlaceholder(tmp) ? pomParser.getProperty(tmp) : tmp;
	}
	
	/**
	 *  Build the generator of mybatis-generator's config file according to project config.
	 */
	private MybatisConfigFileGenerator buildMybatisGeneratorConfigMaker(Project project) {
		PomParser pomParser = new PomParser(project);

		WebContainerParser webContainerParser = new WebContainerParser(project, 
				pomParser.getWebappPaths());

		SpringParser springParser = new SpringParser(project, 
				pomParser.getResourcePaths(), pomParser.getWebappPaths(), 
				webContainerParser.getSpringInitParamValues());
		
		String username = searchPomPropertyIfNecessary(pomParser, springParser.getDatabaseUsername());

		String password = searchPomPropertyIfNecessary(pomParser, springParser.getDatabasePassword());

		String url = searchPomPropertyIfNecessary(pomParser, springParser.getDatabaseUrl());

		String driverClassName = searchPomPropertyIfNecessary(pomParser,
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
	
	
	private void generateFromExistentFile(IFile iFile) {
		IProject iProject = iFile.getProject();
		InputStream inputStream = null;
		EclipseProject project = new EclipseProject(iProject);
		try {
			logger.info("Trying to generate files from existent config file.");
			inputStream = project.getInputStream(MybatisConfigFileGenerator.CONFIG_FILENAME);
			MybatisConfigReader configReader = new MybatisXmlParser(inputStream);
			MybatisFilesWriter filesWriter = new MybatisFilesWriter(project, configReader);
			filesWriter.writeAll();
			logger.info("Generated files.");
		} catch (Exception e) {
			logger.info("No existent configuration found, will generate a new one.");
		} finally {
			project.closeInputStream(inputStream);
		}
	}
	

	private void generateFromProject(IProject project) {
		try {
			logger.info("Scaning project:{}.", project.getName());
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


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelectionService selectionService = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService();
		ISelection selection = selectionService.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (element == null ||
			(!(element instanceof IProject) && !(element instanceof IFile))) {
			return null;
		}
		if (element instanceof IFile) {
			IFile iFile = (IFile)element;
			if (!MybatisConfigFileGenerator.CONFIG_FILENAME.equals(iFile.getName())) {
				return null;
			}
			generateFromExistentFile(iFile);
		} else {
			generateFromProject((IProject)element);
		}
		return null;
	}
}
