package org.nalby.yobatis.mybatis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.log.LogFactory;
import org.nalby.yobatis.log.Logger;
import org.nalby.yobatis.sql.DatabaseMetadataProvider;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.PomTree;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.xml.AbstractXmlParser;

/**
 * Generate MyBaits Generator's configuration file according to current project structure.
 * 
 * @author Kyle Lin
 */
public class MybatisGeneratorXmlCreator implements MybatisGenerator {

	private Document document;

	private PomTree pomTree;
	
	private Element root;

	private Element classPathEntry;
	
	private DocumentFactory factory = DocumentFactory.getInstance();

	private Logger logger = LogFactory.getLogger(MybatisGeneratorXmlCreator.class);
	
	private List<MybatisGeneratorContext> contexts;

	public MybatisGeneratorXmlCreator(PomTree pomTree, 
			DatabaseMetadataProvider sql,
			List<TableGroup> tableGroups) {
		Expect.notNull(pomTree, "pomTree must not be null.");
		Expect.notNull(sql , "sql must not be null.");
		Expect.notNull(tableGroups, "tableGroups must not be null.");
		this.pomTree = pomTree;
		createClassPathEntry(sql);
		createContexts(sql, tableGroups);
		logger.info("Generated MyBatis Generator's configuration file.");
	}
	
	private void createContexts(DatabaseMetadataProvider sql, List<TableGroup> groups) {
		contexts = new ArrayList<>();
		for (TableGroup group : groups) {
			String packageName = FolderUtil.extractPackageName(group.getFolder().path());

			MybatisGeneratorContext thisContext = new MybatisGeneratorContext(packageName, sql);
			thisContext.appendJavaModelGenerator(group.getFolder());

			Folder daoFolder = pomTree.findMostMatchingDaoFolder(group.getFolder());
			thisContext.appendJavaClientGenerator(daoFolder);

			Folder resourceFolder = pomTree.findMostMatchingResourceFolder(group.getFolder());
			thisContext.appendSqlMapGenerator(resourceFolder);

			thisContext.appendTables(group.getTables(), sql.getSchema());
			contexts.add(thisContext);
		}
	}
	
	/**
	 * Get contexts.
	 * @return contexts, or an empty list.
	 */
	public List<MybatisGeneratorContext> getContexts() {
		return contexts;
	}
	
	public Element getClassPathEntryElement() {
		return classPathEntry;
	}
	
	private void createClassPathEntry(DatabaseMetadataProvider sql) {
		classPathEntry = factory.createElement(CLASS_PATH_ENTRY_TAG);
		classPathEntry.addAttribute("location", sql.getConnectorJarPath());
	}

	private void createDocument() {
		document = factory.createDocument();
		DocumentType type = factory.createDocType(ROOT_TAG, 
				"-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN",
				"http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd");
		document.setDocType(type);
	}
	
	@Override
	public String asXmlText() {
		try {
			if (document == null) {
				createDocument();
				root = factory.createElement(ROOT_TAG);
				document.setRootElement(root);
				root.add(classPathEntry);
				for (MybatisGeneratorContext thisContext : contexts) {
					root.add(thisContext.getContext().createCopy());
				}
			}
			return AbstractXmlParser.toXmlString(document);
		} catch (IOException e) {
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
}
