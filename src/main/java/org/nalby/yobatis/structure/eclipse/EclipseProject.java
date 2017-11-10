package org.nalby.yobatis.structure.eclipse;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.DocumentException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.nalby.yobatis.exception.ProjectException;
import org.nalby.yobatis.exception.ProjectNotFoundException;
import org.nalby.yobatis.structure.Project;
import org.nalby.yobatis.structure.SourceCodeFolder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.xml.PomXmlParser;
import org.nalby.yobatis.xml.SpringXmlParser;
import org.nalby.yobatis.xml.WebXmlParser;

public class EclipseProject extends Project {

	private IProject wrappedProject;

	private PomXmlParser pom;

	private SpringXmlParser spring;

	private SourceCodeFolder sourceCodeFolder;

	public EclipseProject(IProject project) {
		this.wrappedProject = project;
		this.root = new EclipseFolder("/",  wrappedProject);
		this.syspath = project.getLocationURI().getPath();
		this.syspath = this.syspath.replace("/" + project.getName(), "");
	}

	@Override
	public String getDatabaseUrl() {
		return spring.getDbUrl();
	}

	@Override
	public String getDatabaseUsername() {
		return spring.getDbUsername();
	}

	@Override
	public String getDatabasePassword() {
		return spring.getDbPassword();
	}

	@Override
	public String getDatabaseDriverClassName() {
		return spring.getDbDriverClass();
	}
	
	@Override
	public String getDatabaseConnectorPath() {
		return pom.dbConnectorJarRelativePath(spring.getDbDriverClass());
	}
	
	@Override
	public String getResourcesDirPath() {
		return wrappedProject.getLocationURI().getPath() + "/" + MAVEN_RESOURCES_PATH.replaceFirst("/$", "");
	}

	@Override
	public String getFullPath() {
		return wrappedProject.getLocationURI().getPath();
	}

	@Override
	public void writeFile(String path, String source) {
		try {
			if (!wrappedProject.isOpen()) {
				wrappedProject.open(null);
			}
			IFile file = wrappedProject.getFile(path);
			file.refreshLocal(0, null);
			if (file.exists()) {
				file.delete(true, false, null);
			}
			InputStream inputStream = new ByteArrayInputStream(source.getBytes());
			file.create(inputStream, IResource.NONE, null);
			file.refreshLocal(0, null);
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new ProjectException(e);
			}
		} catch (CoreException e) {
			throw new ProjectException(e);
		}
	}
	
	public  String concatMavenResitoryPath(String path) {
		Expect.notEmpty(path, "Path should not be null.");
		String home = Platform.getUserLocation().getURL().getPath();
		// Not sure why the '/user' suffix is attached.
		if (home.endsWith("/user/")) {
			home = home.replaceFirst("/user/$", "/.m2/repository/");
		}
		return home + (path.startsWith("/")? path : "/" + path);
	}

	@Override
	public String getDatabaseConnectorFullPath() {
		String home = Platform.getUserLocation().getURL().getPath();
		// Not sure why the '/user' suffix is attached.
		if (home.endsWith("/user/")) {
			home = home.replaceFirst("/user/$", "/.m2/repository/");
		}
		return home + getDatabaseConnectorPath();
	}

	@Override
	public String getModelLayerPath() {
		String path = sourceCodeFolder.modelFolderPath();
		if (path != null) {
			path = path.replace(getSourceCodeDirPath() + "/", "");
		}
		return path;
	}
	
	@Override
	public String getDaoLayerPath() {
		String path = sourceCodeFolder.daoFolderPath();
		if (path != null) {
			path = path.replace(getSourceCodeDirPath() + "/", "");
		}
		return path;
	}

	@Override
	public String getSourceCodeDirPath() {
		return sourceCodeFolder.getPath();
	}
	
	@Override
	public void createDir(String dirPath) {
		try {
			if (!wrappedProject.isOpen()) {
				wrappedProject.open(null);
			}
			IFolder folder = wrappedProject.getFolder(dirPath);
			folder.refreshLocal(0, null);
			if (!folder.exists()) {
				folder.create(true, true, null);
				folder.refreshLocal(0, null);
			}
		} catch (CoreException e) {
			throw new ProjectException(e);
		}
	}

	private static String getServletConfigPath(WebXmlParser webXmlParser) throws DocumentException {
		return null;
		/*Set<String> servletConfigPath = webXmlParser.getServletConfigLocation();
		if (servletConfigPath.size() != 1) {
			throw new ProjectException("Should have only one servlet config.");
		}
		for (String path: servletConfigPath) {
			return path.replace(CLASSPATH_PREFIX, MAVEN_RESOURCES_PATH);
		}
		return null;*/
	}
	
	private static SpringXmlParser getSpringXmlParser(IProject project, WebXmlParser webXmlParser) throws DocumentException, FileNotFoundException, IOException {
		String appConfigPath = null;
		SpringXmlParser springXmlParser = null;
		if (appConfigPath != null) {
			appConfigPath.replace(CLASSPATH_PREFIX, MAVEN_RESOURCES_PATH);
			springXmlParser  = new SpringXmlParser(new FileInputStream(project.getLocationURI().getPath() + "/" + appConfigPath));
		}
		String servletConfigPath = getServletConfigPath(webXmlParser);
		if (servletConfigPath != null) {
			if (springXmlParser != null) {
				//springXmlParser.appendSpringXmlConfig(new FileInputStream(project.getLocationURI().getPath() + "/" + servletConfigPath));
			} else {
				springXmlParser = new SpringXmlParser(new FileInputStream(project.getLocationURI().getPath() + "/" + servletConfigPath));
			}
		}
		if (springXmlParser == null) {
			throw new ProjectException("No spring xml file detected.");
		}
		return springXmlParser;
	}
	
	private static PomXmlParser getPomXmlParser(IProject project) throws FileNotFoundException, DocumentException, IOException {
		PomXmlParser xmlParser = new PomXmlParser(new FileInputStream(project.getLocationURI().getPath() + "/pom.xml"));
		return xmlParser;
	}

	public static EclipseProject build(String name) {
		Expect.notEmpty(name, "project name must not be null.");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(name);
		if (!project.exists()) {
			throw new ProjectNotFoundException();
		}
		try {
			if (project.exists() && !project.isOpen()) {
				project.open(null);
			}

			//WebXmlParser webXmlParser = new WebXmlParser(new FileInputStream(project.getLocationURI().getPath() + "/" + WEB_XML_PATH));
			//IFolder ifolder = project.getFolder(MAVEN_SOURCE_CODE_PATH);
			//SourceCodeFolder sourceCodeFolder = new SourceCodeFolder(new EclipseFolder(null, ifolder));
			//return new EclipseProject(project, getPomXmlParser(project), getSpringXmlParser(project, webXmlParser), sourceCodeFolder);
			return null;
		} catch (Exception e) {
			throw new ProjectException(e.getMessage());
		}
	}


}
