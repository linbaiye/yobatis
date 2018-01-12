package org.nalby.yobatis;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dom4j.DocumentException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.mybatis.generator.api.LibraryRunner;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlCreator;
import org.nalby.yobatis.mybatis.MybatiGeneratorAnalyzer;
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
import org.nalby.yobatis.xml.MybatisGeneratorXmlReader;

public class YobatisGenerationHandler extends AbstractHandler {
	
	private Logger logger = LogFactory.getLogger(this.getClass());
	
	/**
	 *  Build the generator of mybatis-generator's config file according to project config.
	 */
	private MybatisGeneratorXmlCreator buildMybatisGeneratorConfigMaker(Project project) {
		PomTree pomTree = new PomTree(project);

		WebContainerParser webContainerParser = new WebContainerParser(pomTree.getWarPom());

		SpringAntPathFileManager fileManager = new SpringAntPathFileManager(pomTree);

		SpringParser springParser = new SpringParser(fileManager, webContainerParser.searchInitParamValues());

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
		return new MybatisGeneratorXmlCreator(pomTree, builder.build());
	}
	

	private LibraryRunner buildMyBatisRunner(Project project) {
		try {
			File file = project.findFile(MybatiGeneratorAnalyzer.CONFIG_FILENAME);
			try (InputStream inputStream = file.open()) {
				LibraryRunner libraryRunner = new LibraryRunner();
				libraryRunner.parse(inputStream);
				return libraryRunner;
			}
		} catch (Exception e) {
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
	
	private MybatisGeneratorXmlReader buildGeneratorXmlParser(Project project) {
		try {
			File file = project.findFile(MybatiGeneratorAnalyzer.CONFIG_FILENAME);
			try (InputStream inputStream = file.open()) {
				return new MybatisGeneratorXmlReader(inputStream);
			}
		} catch (IOException | DocumentException e) {
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
	
	/*
	 * Merge the new file into the existent one if exists.
	 */
	private MybatiGeneratorAnalyzer mergeIntoExistentConfig(MybatisGeneratorXmlCreator configFileGenerator, Project project) {
		MybatiGeneratorAnalyzer configReader = configFileGenerator;
		File file = project.findFile(MybatisGeneratorXmlCreator.CONFIG_FILENAME);
		if (file != null) {
			try (InputStream inputStream = file.open()) {
				MybatisGeneratorXmlReader mybatisXmlParser = new MybatisGeneratorXmlReader(inputStream);
				mybatisXmlParser.mergeGeneratedConfig(configFileGenerator);
				configReader = mybatisXmlParser;
			} catch (IOException | DocumentException e) {
				logger.info("Unable to merge existent file:{}.", e);
			}
		}
		return configReader;
	}
	

	// When clicked
	private void generateFromExistentFile(IFile iFile) {
		IProject iProject = iFile.getProject();
		EclipseProject project = new EclipseProject(iProject);
		logger.info("Trying to generate files from existent config file.");
		LibraryRunner mybatisRunner = buildMyBatisRunner(project);
		MybatiGeneratorAnalyzer configReader = buildGeneratorXmlParser(project);
		MybatisFilesWriter filesWriter = new MybatisFilesWriter(project, configReader, mybatisRunner);
		filesWriter.writeAll();
	}
	

	private void generateFromProject(IProject iProject) {
		logger.info("Scanning project:{}.", iProject.getName());

		Project project = new EclipseProject(iProject);
		MybatisGeneratorXmlCreator configFileGenerator = buildMybatisGeneratorConfigMaker(project);

		MybatiGeneratorAnalyzer reader = mergeIntoExistentConfig(configFileGenerator, project);

		// Write mybatis-generator's config file to the project's root dir.
		File file = project.createFile(MybatiGeneratorAnalyzer.CONFIG_FILENAME);
		file.write(reader.asXmlText());

		LibraryRunner mybatisRunner = buildMyBatisRunner(project);
		MybatisFilesWriter filesWriter = new MybatisFilesWriter(project, reader, mybatisRunner);
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
			if (!MybatisGeneratorXmlCreator.CONFIG_FILENAME.equals(iFile.getName())) {
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

		SpringParser springParser = new SpringParser(fileManager, webContainerParser.searchInitParamValues());

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
		return null;
	}
}
