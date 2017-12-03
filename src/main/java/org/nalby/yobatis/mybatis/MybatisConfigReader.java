package org.nalby.yobatis.mybatis;

public interface MybatisConfigReader {
	
	/**
	 * The dir path where mapper files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	public String getDaoPath();
	
	/**
	 * The dir path where domain/model files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	public String getDomainPaht();
	
	/**
	 * The dir path where criteria files will be saved.
	 * @return the path.
	 * @throws InvalidMybatisGeneratorConfigException if not configured properly.
	 */
	public String getCriteriaPath();

}
