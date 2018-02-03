package org.nalby.yobatis.mybatis;

public interface MybatisGenerator {

	public final static String CONFIG_FILENAME = "mybatisGeneratorConfig.xml";

	public final static String ROOT_TAG = "generatorConfiguration";
	
	public final static String CLASS_PATH_ENTRY_TAG = "classPathEntry";

	String asXmlText();
}
