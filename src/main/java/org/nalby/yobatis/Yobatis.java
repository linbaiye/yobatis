package org.nalby.yobatis;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dom4j.DocumentException;
import org.mybatis.generator.api.LibraryRunner;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.mybatis.MybatisGeneratorAnalyzer;
import org.nalby.yobatis.mybatis.OldMybatisGeneratorXmlCreator;
import org.nalby.yobatis.mybatis.OldMybatisGeneratorXmlReader;
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
	private static OldMybatisGeneratorXmlCreator buildMybatisGeneratorXmlCreator(Project project) {

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

		return new OldMybatisGeneratorXmlCreator(pomTree, provider, groups);
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
	private static MybatisGeneratorAnalyzer mergeIntoExistentConfig(OldMybatisGeneratorXmlCreator configFileGenerator, Project project) {
		MybatisGeneratorAnalyzer configReader = configFileGenerator;
		File file = project.findFile(OldMybatisGeneratorXmlCreator.CONFIG_FILENAME);
		if (file != null) {
			try (InputStream inputStream = file.open()) {
				OldMybatisGeneratorXmlReader mybatisXmlParser = new OldMybatisGeneratorXmlReader(inputStream);
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

		OldMybatisGeneratorXmlCreator configFileGenerator = buildMybatisGeneratorXmlCreator(project);
		logger.info(configFileGenerator.asXmlText());
/*		MybatisGeneratorAnalyzer analyzer = mergeIntoExistentConfig(configFileGenerator, project);
		// Write mybatis-generator's config file to the project's root dir.
		File file = project.createFile(MybatisGeneratorAnalyzer.CONFIG_FILENAME);
		file.write(analyzer.asXmlText());
		
		// And now run MyBatis Generator.
		LibraryRunner mybatisRunner = buildMyBatisRunner(project);
		MybatisFilesWriter filesWriter = new MybatisFilesWriter(project, analyzer, mybatisRunner);
		filesWriter.writeAll();*/
	}

}
