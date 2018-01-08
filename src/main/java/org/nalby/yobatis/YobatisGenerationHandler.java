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
import org.nalby.yobatis.mybatis.MybatisConfigFileGenerator;
import org.nalby.yobatis.mybatis.MybatisConfigReader;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.sql.mysql.Mysql;
import org.nalby.yobatis.sql.mysql.Mysql.Builder;
import org.nalby.yobatis.structure.LogFactory;
import org.nalby.yobatis.structure.Logger;
import org.nalby.yobatis.structure.PomTree;
import org.nalby.yobatis.structure.Project;
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

		SpringParser springParser = new SpringParser(pomTree, webContainerParser.getSpringInitParamValues());

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
	 * Merge the new file generator into the existent one if exists.
	 */
	private MybatisConfigReader mergeIntoExistentConfig(MybatisConfigFileGenerator configFileGenerator, Project project) {
		MybatisConfigReader reader = configFileGenerator;
		try (InputStream inputStream = project.openFile(MybatisConfigReader.CONFIG_FILENAME)) {
			MybatisXmlParser mybatisXmlParser = new MybatisXmlParser(inputStream);
			mybatisXmlParser.mergeGeneratedConfigAndGetXmlString(configFileGenerator);
			reader = mybatisXmlParser;
		} catch (Exception e) {
			logger.info("No existent configuration found, will generate a new one.");
		}
		return reader;
	}
	
	
	private void generateFromExistentFile(IFile iFile) {
		IProject iProject = iFile.getProject();
		EclipseProject project = new EclipseProject(iProject);
		try (InputStream inputStream = project.openFile(MybatisConfigFileGenerator.CONFIG_FILENAME)) {
			logger.info("Trying to generate files from existent config file.");
			MybatisConfigReader configReader = new MybatisXmlParser(inputStream);
			MybatisFilesWriter filesWriter = new MybatisFilesWriter(project, configReader);
			filesWriter.writeAll();
			logger.info("Generated files.");
		} catch (Exception e) {
			logger.info("No existent configuration found, will generate a new one.");
		}
	}
	

	private void generateFromProject(IProject project) {
		try {
			logger.info("Scaning project:{}.", project.getName());
			EclipseProject eclipseProject = new EclipseProject(project);

			MybatisConfigFileGenerator configFileGenerator = buildMybatisGeneratorConfigMaker(eclipseProject);

			MybatisConfigReader reader = mergeIntoExistentConfig(configFileGenerator, eclipseProject);

			//Write mybatis-generator's config file to the project's root dir.
			eclipseProject.writeFile(MybatisConfigReader.CONFIG_FILENAME, reader.asXmlText());

			MybatisFilesWriter filesWriter = new MybatisFilesWriter(eclipseProject, reader);
			filesWriter.writeAll();
			logger.info("Generated files.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Caught exception:{}.", e.getMessage());
		}
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

		SpringParser springParser = new SpringParser(pomTree, webContainerParser.getSpringInitParamValues());

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
			System.out.println("\t keys:");
			for (String key : table.getPrimaryKey()) {
				System.out.println("\t\t key:" + key );
			}
			System.out.println("\t auto inc columns:");
			for (String key : table.getAutoIncColumn()) {
				System.out.println("\t\t column:" + key);
			}
		}
		//MybatisConfigFileGenerator generator = new MybatisConfigFileGenerator(pomTree, builder.build());
		//System.out.println(generator.asXmlText());
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		start();
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
