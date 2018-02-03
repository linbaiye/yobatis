package org.nalby.yobatis;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.mybatis.generator.api.LibraryRunner;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.mybatis.MybatisFilesWriter;
import org.nalby.yobatis.mybatis.MybatisGenerator;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlCreator;
import org.nalby.yobatis.mybatis.MybatisGeneratorXmlReader;
import org.nalby.yobatis.mybatis.TableGroup;
import org.nalby.yobatis.mybatis.TokenSimilarityTableGrouper;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.sql.mysql.MysqlDatabaseMetadataProvider;
import org.nalby.yobatis.sql.mysql.MysqlDatabaseMetadataProvider.Builder;
import org.nalby.yobatis.structure.File;
import org.nalby.yobatis.structure.Folder;
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

		SpringParser springParser = new SpringParser(fileManager, 
				webContainerParser.searchInitParamValues());

		String driverClassName = springParser.getDatabaseDriverClassName();

		Builder builder = MysqlDatabaseMetadataProvider.builder();
		builder.setConnectorJarPath(pomTree.getDatabaseJarPath(driverClassName))
		.setDriverClassName(springParser.getDatabaseDriverClassName())
		.setUsername(springParser.getDatabaseUsername())
		.setPassword(springParser.getDatabaseUsername())
		.setUrl(springParser.getDatabaseUrl());

		DatabaseMetadataProvider provider = builder.build();

		List<Folder> modelFolders = pomTree.lookupModelFolders();
		TokenSimilarityTableGrouper grouper = new TokenSimilarityTableGrouper(modelFolders);
		List<TableGroup> groups = grouper.group(provider.getTables());

		return new MybatisGeneratorXmlCreator(pomTree, provider, groups);
	}
	

	private static LibraryRunner buildMyBatisRunner(Project project) {
		try {
			File file = project.findFile(MybatisGenerator.CONFIG_FILENAME);
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
	private static MybatisGenerator mergeIntoExistentConfig(MybatisGeneratorXmlCreator configFileGenerator, Project project) {
		MybatisGenerator generator = configFileGenerator;
		File file = project.findFile(MybatisGenerator.CONFIG_FILENAME);
		if (file != null) {
			try (InputStream inputStream = file.open()) {
				MybatisGeneratorXmlReader mybatisXmlParser = MybatisGeneratorXmlReader.build(inputStream);
				mybatisXmlParser.mergeGeneratedConfig(configFileGenerator);
				generator = mybatisXmlParser;
			} catch (IOException  e) {
				logger.info("Unable to merge existent file:{}.", e);
			}
		}
		file = project.createFile(MybatisGenerator.CONFIG_FILENAME);
		file.write(generator.asXmlText());
		return generator;
	}

	public static void onClickProject(Project project) {
		logger.info("Scanning project:{}.", project.name());
		MybatisGeneratorXmlCreator generator = buildMybatisGeneratorXmlCreator(project);
		mergeIntoExistentConfig(generator, project);
		logger.info("Config file has been created, right-click on {} to generate java and xml mapper files.", MybatisGenerator.CONFIG_FILENAME);
	}
	
	public static void onClickFile(Project project) {
		logger.info("Using existent config file.");
		LibraryRunner runner = buildMyBatisRunner(project);
		new MybatisFilesWriter(project, runner).writeAll();;
	}

}
