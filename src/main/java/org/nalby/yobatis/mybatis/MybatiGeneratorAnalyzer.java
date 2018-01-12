package org.nalby.yobatis.mybatis;


public interface MybatiGeneratorAnalyzer {
	
	public final static String CONFIG_FILENAME = "mybatisGeneratorConfig.xml";
	
	public final static String YOBATIS_PLUGIN = "org.mybatis.generator.plugins.YobatisPlugin";
	
	public final static String YOBATIS_CRITERIA_PLUGIN = "org.mybatis.generator.plugins.YobatisCriteriaPlugin";
	
	public final static String DEFAULT_CONTEXT_ID = "yobatis";

	public final static String TARGET_RUNTIME = "MyBatis3";
	
	/**
	 * The dir path where java mapper files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	String getDaoDirPath();
	
	/**
	 * The dir path where java domain/model files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	String getDomainDirPath();
	
	/**
	 * The dir path where java criteria files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	String getCriteriaDirPath();
	
	
	/**
	 * Get the targetPackage value of the javaModelGenerator.
	 * @return the value
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	String getPackageNameOfDomains();
	
	
	/**
	 * Get the targetPackage value of the javaClientGenerator.
	 * @return the value
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	String getPackageNameOfJavaMappers();
	
	/**
	 * The dir path where xml mapper files will be saved.
	 * @return the dir path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	String getXmlMapperDirPath();
	
	/**
	 * Turn this config into a text string.
	 * @return this content string.
	 */
	String asXmlText();

}
