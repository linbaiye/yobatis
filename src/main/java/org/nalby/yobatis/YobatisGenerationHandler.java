package org.nalby.yobatis;

import java.io.InputStream;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;
import org.nalby.yobatis.mybatis.MybatisConfigReader;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.sql.mysql.Mysql.Builder;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.structure.PomTree;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.SpringAntPathFileManager;
import org.nalby.yobatis.structure.SpringParser;
import org.nalby.yobatis.structure.WebContainerParser;
import org.nalby.yobatis.structure.eclipse.EclipseProject;
import org.nalby.yobatis.xml.MybatisXmlParser;

public class YobatisGenerationHandler extends AbstractHandler {
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	/**
	 *  Build the generator of mybatis-generator's config file according to project config.
	 */
	private MybatisConfigFileGenerator buildMybatisGeneratorConfigMaker(Project project) {
		PomTree pomTree = new PomTree(project);

		WebContainerParser webContainerParser = new WebContainerParser(pomTree.getWarPom());

		SpringAntPathFileManager fileManager = new SpringAntPathFileManager(pomTree);

		SpringParser springParser = new SpringParser(fileManager, webContainerParser.getSpringInitParamValues());

		String username = springParser.getDatabaseUsername();

		String password = springParser.getDatabasePassword();

		String url = springParser.getDatabaseUrl();

		String driverClassName = springParser.getDatabaseDriverClassName();

		String dbJarPath = pomTree.getDatabaseJarPath(driverClassName);

		Builder builder = Mysql.builder();
		builder.setConnectorJarPath(dbJarPath)
		.setDriverClassName(driverClassName)
		.setUsername(username)
		.setPassword(password)
		.setUrl(url);
		return new MybatisConfigFileGenerator(pomTree, builder.build());
	}
	
	/*
	 * Merge the new file into the existent one if exists.
	 */
	private MybatisConfigReader mergeIntoExistentConfig(MybatisConfigFileGenerator configFileGenerator, Project project) {
		MybatisConfigReader configReader = configFileGenerator;
		File file = project.findFile(MybatisConfigFileGenerator.CONFIG_FILENAME);
		if (file == null) {
			return configReader;
		}
		try (InputStream inputStream = file.open()) {
			MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(inputStream);
			mybatisXmlParser.mergeGeneratedConfig(configFileGenerator);
			configReader = mybatisXmlParser;
		} catch (Exception e) {
			logger.info("Unable to merge existent file:{}.", e);
		}
		return configReader;
	}
	
	

	private void generateFromExistentFile(IFile iFile) {
		IProject iProject = iFile.getProject();
		EclipseProject project = new EclipseProject(iProject);
		try {
			File file = project.findFile(MybatisConfigFileGenerator.CONFIG_FILENAME);
			logger.info("Trying to generate files from existent config file.");
			try (InputStream inputStream = file.open()) {
				MybatisConfigReader configReader = new MybatisXmlParser(inputStream);
				MybatisFilesWriter filesWriter = new MybatisFilesWriter(project, configReader);
				filesWriter.writeAll();
			}
		} catch (Exception e) {
			logger.info("No existent configuration found, will generate a new one.");
		}
	}
	

	private void generateFromProject(IProject iProject) {
		logger.info("Scaning project:{}.", iProject.getName());
		Project project = new EclipseProject(iProject);

		MybatisConfigFileGenerator configFileGenerator = buildMybatisGeneratorConfigMaker(project);

		MybatisConfigReader reader = mergeIntoExistentConfig(configFileGenerator, project);

		// Write mybatis-generator's config file to the project's root dir.
		File file = project.createFile(MybatisConfigReader.CONFIG_FILENAME);
		file.write(reader.asXmlText());

		MybatisFilesWriter filesWriter = new MybatisFilesWriter(project, reader);
		filesWriter.writeAll();
	}
	
	
	private Object start() {
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


	private void buildMybatisGeneratorConfigMaker1(Project project) {
		PomTree pomTree = new PomTree(project);

		WebContainerParser webContainerParser = new WebContainerParser(pomTree.getWarPom());

		SpringAntPathFileManager fileManager = new SpringAntPathFileManager(pomTree);

		SpringParser springParser = new SpringParser(fileManager, webContainerParser.getSpringInitParamValues());

		String username = springParser.getDatabaseUsername();

		String password = springParser.getDatabasePassword();

		String url = springParser.getDatabaseUrl();

		String driverClassName = springParser.getDatabaseDriverClassName();

		String dbJarPath = pomTree.getDatabaseJarPath(driverClassName);

		Builder builder = Mysql.builder();
		builder.setConnectorJarPath(dbJarPath)
		.setDriverClassName(driverClassName)
		.setUsername(username)
		.setPassword(password)
		.setUrl(url);
		List<Table> tables = builder.build().getTables();
		for (Table table : tables) {
			System.out.println(table.getName() + ":");
		}
		//MybatisConfigFileGenerator generator = new MybatisConfigFileGenerator(pomTree, builder.build());
		//System.out.println(generator.asXmlText());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			start();
		} catch (Exception e) {
			logger.error("Caught exception:{}.", e);
		}
/*		ISelectionService selectionService = PlatformUI.getWorkbench()
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
		if (element instanceof IProject) {
			IProject iProject = (IProject) element;
			EclipseProject project = new EclipseProject(iProject);
			PomTree pomTree = new PomTree(project);
			Pom pom = pomTree.getWarPom();
			WebContainerParser webContainerParser = new WebContainerParser(pom);
			SpringParser springParser = new SpringParser(pomTree, 
					webContainerParser.getSpringInitParamValues());
			System.out.println(springParser.getDatabasePassword());

			String driverClassName = springParser.getDatabaseDriverClassName();

			String dbJarPath = pomTree.getDatabaseJarPath(driverClassName);

			try {
			buildMybatisGeneratorConfigMaker1(project);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(dbJarPath);
		}*/
		return null;
	}
}
