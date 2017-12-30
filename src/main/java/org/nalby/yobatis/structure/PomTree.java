package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.nalby.yobatis.exception.InvalidConfigurationException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.structure.Project.FolderSelector;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.util.TextUtil;
import org.nalby.yobatis.xml.PomXmlParser;

/**
 * This class is responsible for parsing the pom tree. Also it will
 * find out the entry pom, the WAR packaging currently.
 * 
 * @author Kyle Lin
 */
public class PomTree {

	private Project project;

	private Pom root;

	private Pom webpom;
	
	private Set<Pom> poms;
	
	public PomTree(Project project) {
		try {
			this.project = project;
			this.root = new PomNode(null, this.project);
			findAllPoms();
		} catch (Exception e) {
			throw new UnsupportedProjectException(e);
		}
	}

	private class PomNode implements Pom {
		private PomXmlParser pomXmlParser;

		private List<Pom> children;

		private Pom parent;

		private Folder folder;
		
		private Folder sourceCodeFolder;
		
		/**
		 * The webapp folder if exists.
		 */
		private Folder webappFolder;
		/**
		 * The resource folders in this module.
		 */
		private Set<Folder> resourceFolders;

		private PomNode(Pom parent, Folder folder) {
			Expect.notNull(folder, "folder is null.");
			this.parent = parent;
			this.folder = folder;
			children = new LinkedList<Pom>();
			readPomFile();
			buildSubtree();
			loadResourceAndWebappFolders();
		}
		
		public PomXmlParser getParser() {
			return pomXmlParser;
		}
		
		public List<Pom> getChildren() {
			return children;
		}

		private void buildSubtree() {
			Set<String> moduleNames = pomXmlParser.getModuleNames();
			for (String module : moduleNames) {
				Folder subfolder = folder.findFolder(module);
				children.add(new PomNode(this, subfolder));
			}
		}
		
		private void loadResourceAndWebappFolders() {
			if (isWar()) {
				webappFolder = this.folder.findFolder("src/main/webapp");
			}
			resourceFolders = new HashSet<Folder>();
			if (isContainer()) {
				return;
			}
			sourceCodeFolder = this.folder.findFolder("src/main/java");
			Set<String> paths = pomXmlParser.getResourceDirs();
			for (String path: paths) {
				Folder folder = this.folder.findFolder(path);
				if (folder != null) {
					resourceFolders.add(folder);
				}
			}
		}

		private void readPomFile() {
			InputStream inputStream = null;
			try {
				inputStream = folder.openInputStream("pom.xml");
				pomXmlParser = new PomXmlParser(inputStream);
			} catch (Exception e) {
				throw new InvalidConfigurationException(e);
			} finally {
				FolderUtil.closeStream(inputStream);
			}
		}

		@Override
		public boolean isWar() {
			return pomXmlParser.isPackagingWar();
		}

		@Override
		public boolean isContainer() {
			return pomXmlParser.isContainer();
		}

		@Override
		public Folder getFolder() {
			return folder;
		}

		@Override
		public Set<Folder> getResourceFolders() {
			return resourceFolders;
		}

		@Override
		public Folder getWebappFolder() {
			return webappFolder;
		}

		@Override
		public String filterPlaceholders(String text) {
			if (TextUtil.isEmpty(text)) {
				return text;
			}
			List<String> placeholders = PropertyUtil.placeholdersFrom(text);
			for (String placeholder: placeholders) {
				String key = PropertyUtil.valueOfPlaceholder(placeholder);
				String val = pomXmlParser.getProperty(key);
				if (val != null) {
					text = text.replace(placeholder, val);
				}
			}
			if (!placeholders.isEmpty() && parent != null) {
				return parent.filterPlaceholders(text);
			}
			return text;
		}

		@Override
		public Folder getSourceCodeFolder() {
			return sourceCodeFolder;
		}
	}
	
	public Set<Pom> getPoms() {
		return poms;
	}
	
	private interface PomSelector {
		boolean selectPom(Pom node);
	}
	
	private Pom treeWalker(PomSelector selector) {
		Stack<Pom> stack = new Stack<Pom>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Pom node = stack.pop();
			if (selector.selectPom(node)) {
				return node;
			}
			for (Pom item : ((PomNode)node).getChildren()) {
				stack.push(item);
			}
		}
		return null;
	}
	
	private void findAllPoms() {
		poms = new HashSet<Pom>();
		treeWalker(new PomSelector() {
			@Override
			public boolean selectPom(Pom node) {
				poms.add(node);
				return false;
			}
		});
		webpom = treeWalker(new PomSelector() {
			@Override
			public boolean selectPom(Pom node) {
				return node.isWar();
			}
		});
	}
	
	public Pom getWarPom() {
		return webpom;
	}
	

	/**
	 * Get sql connector's jar path based on the {@code driverClassName}, the
	 * first <dependency> will be used if multiple found.
	 * 
	 * @param driverClassName
	 *            the sql's driver class name.
	 * @return the system path of the connector, null if not found.
	 */
	public String getDatabaseJarPath(String driverClassName) {
		for (Pom node: poms) {
			PomXmlParser parser = ((PomNode)node).getParser();
			String tmp = parser.dbConnectorJarRelativePath(driverClassName);
			if (tmp != null) {
				return project.concatMavenResitoryPath(tmp);
			}
		}
		return null;
	}
	
	private List<Folder> sourceCodeFolders = null;

	private List<Folder> lookupSourceCodeFolders() {
		if (sourceCodeFolders != null) {
			return sourceCodeFolders;
		}
		sourceCodeFolders =  new LinkedList<Folder>();
		for (Pom pom : getPoms()) {
			Folder tmp = pom.getSourceCodeFolder();
			if (tmp != null) {
				sourceCodeFolders.add(tmp);
			}
		}
		return sourceCodeFolders;
	}
	
	private List<Folder> iterateSourceCodeFolders(FolderSelector selector) {
		List<Folder> sourceCodeFolders = lookupSourceCodeFolders();
		List<Folder> folders = new LinkedList<Folder>();
		for (Folder sourceCodeFolder: sourceCodeFolders) {
			for (Folder folder: sourceCodeFolder.getAllFolders()) {
				if (selector.isSelected(folder)) {
					folders.add(folder);
				}
			}
		}
		return folders;
	}
	
	public List<Folder> lookupDaoFolders() {
		return iterateSourceCodeFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				String path = folder.path();
				if (path.endsWith("dao") || path.endsWith("mapper") || path.endsWith("repository")) {
					return true;
				}
				return false;
			}
		});
	}
	
	public List<Folder> lookupModelFolders() {
		return iterateSourceCodeFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				String path = folder.path();
				if (path.endsWith("domain") || path.endsWith("entity") || path.endsWith("model")) {
					return true;
				}
				return false;
			}
		});
	}
	
	public List<Folder> lookupResourceFolders() {
		List<Folder> resourceFolders = new LinkedList<Folder>();
		for (Pom pom: poms) {
			resourceFolders.addAll(pom.getResourceFolders());
		}
		return resourceFolders;
	}

}
