package org.nalby.yobatis.mybatis;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.nalby.yobatis.exception.InvalidMybatisGeneratorConfigException;
import org.nalby.yobatis.util.Expect;

/**
 * This class allows the code generator to be run from the command line.
 * 
 */
public class MybatisRunner {

	
	private List<GeneratedXmlFile> xmlFiles;

	private List<GeneratedJavaFile> javaFiles;
	
	@SuppressWarnings("unchecked")
	private MybatisRunner(List<GeneratedXmlFile> xmlFiles, 
			List<GeneratedJavaFile> javaFiles) {
		this.xmlFiles = xmlFiles == null ? Collections.EMPTY_LIST : xmlFiles;
		this.javaFiles = javaFiles == null ? Collections.EMPTY_LIST : javaFiles;
	}

	public List<GeneratedXmlFile> getGeneratedXmlFiles() {
		return xmlFiles;
	}

	public List<GeneratedJavaFile> getGeneratedJavaFiles() {
		return javaFiles;
	}
	
	public static MybatisRunner parse(InputStream inputStream) {
		Expect.notNull(inputStream, "inputstream must not be empty.");
		try {
			List<String> warnings = new ArrayList<String>();
			ConfigurationParser cp = new ConfigurationParser(warnings);
			Configuration config = cp.parseConfiguration(inputStream);
			DefaultShellCallback shellCallback = new DefaultShellCallback(false);
			MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
			myBatisGenerator.generate(null, null, null, false);
			return new MybatisRunner(myBatisGenerator.getGeneratedXmlFiles(),
					myBatisGenerator.getGeneratedJavaFiles());
		} catch (Exception e) {
			throw new InvalidMybatisGeneratorConfigException(e);
		}
	}

}

