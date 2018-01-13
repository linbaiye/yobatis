package org.nalby.yobatis;

import java.io.IOException;
import java.io.InputStream;

import org.dom4j.DocumentException;
import org.mybatis.generator.api.LibraryRunner;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.mybatis.MybatisGeneratorAnalyzer;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlCreator;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlReader;
import org.nalby.yobatis.sql.mysql.MysqlDatabaseMetadataProvider;
import org.nalby.yobatis.sql.mysql.MysqlDatabaseMetadataProvider.Builder;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.structure.PomTree;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.SpringAntPathFileManager;
import org.nalby.yobatis.structure.SpringParser;
import org.nalby.yobatis.structure.WebContainerParser;

public class Yobatis {

	private static Logger logger = LogFactory.getLogger(Yobatis.class);
	
	/**
	 *  Build the generator of mybatis-generator's config file according to project config.
	 */
	private static MybatisGeneratorXmlCreator buildMybatisGeneratorXmlCreator(Project project) {
		PomTree pomTree = new PomTree(project);

		WebContainerParser webContainerParser = new WebContainerParser(pomTree.getWarPom());

		SpringAntPathFileManager fileManager = new SpringAntPathFileManager(pomTree);

		SpringParser springParser = new SpringParser(fileManager, webContainerParser.searchInitParamValues());

		String username = springParser.getDatabaseUsername();

		String password = springParser.getDatabasePassword();

		String url = springParser.getDatabaseUrl();

		String driverClassName = springParser.getDatabaseDriverClassName();

		String dbJarPath = pomTree.getDatabaseJarPath(driverClassName);

		Builder builder = MysqlDatabaseMetadataProvider.builder();
		builder.setConnectorJarPath(dbJarPath)
		.setDriverClassName(driverClassName)
		.setUsername(username)
		.setPassword(password)
		.setUrl(url);
		return new MybatisGeneratorXmlCreator(pomTree, builder.build());
	}
	

	private static LibraryRunner buildMyBatisRunner(Project project) {
		try {
			File file = project.findFile(MybatisGeneratorAnalyzer.CONFIG_FILENAME);
			try (InputStream inputStream = file.open()) {
				LibraryRunner libraryRunner = new LibraryRunner();
				libraryRunner.parse(inputStream);
				return libraryRunner;
			}
		} catch (Exception e) {
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
	
	/*
	 * Merge the new file into the existent one if exists.
	 */
	private static MybatisGeneratorAnalyzer mergeIntoExistentConfig(MybatisGeneratorXmlCreator configFileGenerator, Project project) {
		MybatisGeneratorAnalyzer configReader = configFileGenerator;
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


	public static void generate(Project project) {
		logger.info("Scanning project:{}.", project.name());

		MybatisGeneratorXmlCreator configFileGenerator = buildMybatisGeneratorXmlCreator(project);
		MybatisGeneratorAnalyzer analyzer = mergeIntoExistentConfig(configFileGenerator, project);
		// Write mybatis-generator's config file to the project's root dir.
		File file = project.createFile(MybatisGeneratorAnalyzer.CONFIG_FILENAME);
		file.write(analyzer.asXmlText());
		
		// And now run MyBatis Generator.
		LibraryRunner mybatisRunner = buildMyBatisRunner(project);
		MybatisFilesWriter filesWriter = new MybatisFilesWriter(project, analyzer, mybatisRunner);
		filesWriter.writeAll();
	}

}
