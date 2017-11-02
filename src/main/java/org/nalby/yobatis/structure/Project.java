package org.nalby.yobatis.structure;

public abstract class Project {
	
	protected final static String MAVEN_SOURCE_CODE_PATH = "src/main/java/";

	protected final static String MAVEN_RESOURCES_PATH = "src/main/resources/";

	protected final static String WEB_XML_PATH = "src/main/webapp/WEB-INF/web.xml";
	
	protected final static String CLASSPATH_PREFIX = "classpath:";
	

	public abstract String getDatabaseUrl();
	
	public abstract String getDatabaseUsername();
	
	public abstract String getDatabasePassword();
	
	public abstract String getDatabaseDriverClassName();
	
	public abstract String getDatabaseConnectorPath();

	public abstract String getDatabaseConnectorFullPath();
	
	public abstract String getSourceCodeDirPath();

	public abstract String getResourcesDirPath();
	
	public abstract String getModelLayerPath();

	public abstract String getDaoLayerPath();

	public abstract String getFullPath();
	
	public abstract void writeFile(String path, String source);
	
	public abstract void createDir(String dirPath);
	
}
