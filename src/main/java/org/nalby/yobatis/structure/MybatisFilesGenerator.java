package org.nalby.yobatis.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.LibraryRunner;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.sql.Sql;

/**
 * Used to generate files relevant to mybatis.
 * @author Kyle Lin
 */
public class MybatisFilesGenerator {
	
	private Project project;
	
	private Document document;
	
	private Sql sql;

	private final static String CONFIG_PATH = "mybatisGeneratorConfig.xml";
	
	private DocumentFactory factory = DocumentFactory.getInstance();
	
	private LibraryRunner mybatisRunner;
	
	public MybatisFilesGenerator(Project project, Sql sql, LibraryRunner runner) {
		this.project = project;
		this.sql = sql;
		this.mybatisRunner = runner;
	}
	
	private void appendClassPathEntry(Element root) {
		Element classPathEntry = factory.createElement("classPathEntry");
		classPathEntry.addAttribute("location", project.getDatabaseConnectorFullPath());
		root.add(classPathEntry);
	}
	
	private void appendTypeResolver(Element root) {
		Element typeResolver = factory.createElement("javaTypeResolver");
		Element property = factory.createElement("property");
		property.addAttribute("name", "forceBigDecimals");
		property.addAttribute("value", "false");
		typeResolver.add(property);
		root.add(typeResolver);
	}
	
	private String convertToString() throws IOException {
	    OutputFormat format = OutputFormat.createPrettyPrint();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos, true, "utf-8");
        XMLWriter writer = new XMLWriter(ps, format);
        writer.write(document);
	    String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
	    ps.close();
	    return content;
	}
	
	private void appendTables(Element context)  {
		List<String> names = sql.getTableNames();
		for (String name: names) {
			Element table = factory.createElement("table");
			table.addAttribute("tableName", name);
			table.addAttribute("schema", sql.getSchema());
			context.add(table);
		}
	}
	
	private void appendJdbcConnection(Element root) {
		Element jdbConnection = factory.createElement("jdbcConnection");
		jdbConnection.addAttribute("driverClass", project.getDatabaseDriverClassName());
		jdbConnection.addAttribute("connectionURL", project.getDatabaseUrl());
		jdbConnection.addAttribute("userId", project.getDatabaseUsername());
		jdbConnection.addAttribute("password", project.getDatabasePassword());
		root.add(jdbConnection);
	}

	private void appendJavaModelGenerator(Element context) {
		Element javaModelGenerator = factory.createElement("javaModelGenerator");
		String path = project.getModelLayerPath();
		javaModelGenerator.addAttribute("targetPackage", path == null ? "" : pathToPackage(path));
		path = project.getSourceCodeDirPath();
		javaModelGenerator.addAttribute("targetProject", path == null ? "" : path);
		context.add(javaModelGenerator);
	}
	
	private String pathToPackage(String path) {
		return path.replaceAll("/", ".");
	}
	
	private String packageToPath(String packageName) {
		return packageName.replaceAll("\\.", "/");
	}
	
	private Element appendContext(Element root) {
		Element context = factory.createElement("context");
		context.addAttribute("id", "mysqlTables");
		context.addAttribute("targetRuntime", "MyBatis3");
		root.add(context);
		return context;
	}
	
	private void appendSqlMapGenerator(Element context) {
		Element generator = context.addElement("sqlMapGenerator");
		generator.addAttribute("targetPackage", "mybatis-mappers/autogen");
		generator.addAttribute("targetProject", project.getResourcesDirPath());
	}
	
	private void appendJavaClientGenerator(Element context) {
		Element generator = context.addElement("javaClientGenerator");
		generator.addAttribute("type", "XMLMAPPER");
		String path = project.getDaoLayerPath();
		generator.addAttribute("targetPackage", path == null ? "" : pathToPackage(path));
		path = project.getSourceCodeDirPath();
		generator.addAttribute("targetProject", path == null ? "" : path);
	}
	
	private void createDocument() {
		document = factory.createDocument();
		DocumentType type = factory.createDocType("generatorConfiguration", "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN", "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd");
		document.setDocType(type);
	}

	public void writeJavaFiles() {
		String modelPath = project.getModelLayerPath();
		String patternStr =  "import " + (modelPath == null? "": pathToPackage(modelPath)) + "\\.(.+Example);";
		Pattern pattern = modelPath == null? null : Pattern.compile(patternStr);
		List<GeneratedJavaFile> list = mybatisRunner.getGeneratedJavaFiles();
		for (GeneratedJavaFile javaFile : list) {
			String dir = javaFile.getTargetProject().replace(project.getFullPath(), "") + packageToPath(javaFile.getTargetPackage());
			String filePath = dir  + "/" + javaFile.getFileName();
			String content = javaFile.getFormattedContent();
			if (javaFile.getFileName().endsWith("Example.java")) {
				project.createDir(dir + "/criteria");
				filePath = dir + "/criteria/" + javaFile.getFileName();
				content = content.replace("package " + javaFile.getTargetPackage(), "package " + javaFile.getTargetPackage() + ".criteria");
			} else if (javaFile.getFileName().endsWith("Mapper.java") && pattern != null) {
				Matcher matcher = pattern.matcher(content);
				if (matcher.find()) {
					String name = matcher.group(1);
					content = content.replaceAll(patternStr, "import " + pathToPackage(modelPath) + ".criteria." + name + ";");
				}
			}
			project.writeFile(filePath, content);
		}
	}

	private void writeConfigFile() {
		createDocument();
		Element root = factory.createElement("generatorConfiguration");
		appendClassPathEntry(root);
		document.setRootElement(root);

		Element context = appendContext(root);
		appendJdbcConnection(context);
		appendTypeResolver(context);
		appendJavaModelGenerator(context);
		appendSqlMapGenerator(context);
		appendJavaClientGenerator(context);
		appendTables(context);
		try {
			String content = convertToString();
			project.writeFile(CONFIG_PATH, content);
		} catch (Exception e) {
			throw new ProjectException(e);
		}
	}
	
	public void writeAllFiles() {
		try {
			writeConfigFile();
			mybatisRunner.parse(project.getFullPath() + "/" + CONFIG_PATH);
			writeJavaFiles();
		} catch (InvalidConfigurationException e) {
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}
	
}
