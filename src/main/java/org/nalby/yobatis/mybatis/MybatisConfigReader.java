package org.nalby.yobatis.mybatis;

public interface MybatisConfigReader {
	
	/**
	 * The dir path where mapper files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	public String getDaoDirPath();
	
	/**
	 * The dir path where domain/model files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	public String getDomainDirPath();
	
	/**
	 * The dir path where criteria files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	public String getCriteriaDirPath();
	
	/**
	 * Get the generator's config filename.
	 * @return the filename.
	 */
	public String getConfigeFilename();
	
	/**
	 * Get the targetPackage value of the javaModelGenerator.
	 * @return the value
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	public String getPackageNameOfDomains();
	
	/**
	 * The dir path where xml mapper files will be saved..
	 * @return the dir path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	public String getMapperDirPath();

}
