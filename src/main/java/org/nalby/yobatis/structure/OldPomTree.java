package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.nalby.yobatis.exception.InvalidConfigurationException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.util.TextUtil;
import org.nalby.yobatis.xml.PomXmlParser;

/**
 * PomTree is responsible for parsing the pom tree. Also it will
 * find out the entry pom, source code folders, dao folders, resource dirs.
 * 
 * @author Kyle Lin
 */
public class OldPomTree {

	private OldProject project;

	private OldPom root;

	private OldPom webpom;
	
	private Set<OldPom> poms;
	
	public OldPomTree(OldProject project) {
		try {
			this.project = project;
			this.root = new PomNode(null, this.project);
			findAllPoms();
		} catch (Exception e) {
			throw new UnsupportedProjectException(e);
		}
	}

	private class PomNode implements OldPom {
		private PomXmlParser pomXmlParser;

		private List<OldPom> children;

		private OldPom parent;

		private OldFolder folder;
		
		private OldFolder sourceCodeFolder;
		
		/**
		 * The webapp folder if exists.
		 */
		private OldFolder webappFolder;
		/**
		 * The resource folders in this module.
		 */
		private Set<OldFolder> resourceFolders;

		private PomNode(OldPom parent, OldFolder folder) {
			Expect.notNull(folder, "folder is null.");
			this.parent = parent;
			this.folder = folder;
			children = new LinkedList<OldPom>();
			readPomFile();
			buildSubtree();
			loadResourceAndWebappFolders();
		}
		
		public PomXmlParser getParser() {
			return pomXmlParser;
		}
		
		public List<OldPom> getChildren() {
			return children;
		}

		private void buildSubtree() {
			Set<String> moduleNames = pomXmlParser.getModuleNames();
			for (String module : moduleNames) {
				OldFolder subfolder = folder.findFolder(module);
				children.add(new PomNode(this, subfolder));
			}
		}
		
		private void loadResourceAndWebappFolders() {
			if (isWar()) {
				webappFolder = this.folder.findFolder("src/main/webapp");
			}
			resourceFolders = new HashSet<OldFolder>();
			if (isContainer()) {
				return;
			}
			sourceCodeFolder = this.folder.findFolder("src/main/java");
			Set<String> paths = pomXmlParser.getResourceDirs();
			for (String path: paths) {
				OldFolder folder = this.folder.findFolder(path);
				if (folder != null) {
					resourceFolders.add(folder);
				}
			}
		}

		private void readPomFile() {

			try (InputStream inputStream = folder.openFile("pom.xml")) {
				pomXmlParser = new PomXmlParser(inputStream);
			} catch (Exception e) {
				throw new InvalidConfigurationException(e);
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
		public Set<OldFolder> getResourceFolders() {
			return resourceFolders;
		}

		@Override
		public OldFolder getWebappFolder() {
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
		public OldFolder getSourceCodeFolder() {
			return sourceCodeFolder;
		}
	}
	
	public Set<OldPom> getPoms() {
		return poms;
	}
	
	private interface PomSelector {
		boolean selectPom(OldPom node);
	}
	
	private OldPom treeWalker(PomSelector selector) {
		Stack<OldPom> stack = new Stack<OldPom>();
		stack.push(root);
		while (!stack.isEmpty()) {
			OldPom node = stack.pop();
			if (selector.selectPom(node)) {
				return node;
			}
			for (OldPom item : ((PomNode)node).getChildren()) {
				stack.push(item);
			}
		}
		return null;
	}
	
	private void findAllPoms() {
		poms = new HashSet<OldPom>();
		treeWalker(new PomSelector() {
			@Override
			public boolean selectPom(OldPom node) {
				poms.add(node);
				return false;
			}
		});
		webpom = treeWalker(new PomSelector() {
			@Override
			public boolean selectPom(OldPom node) {
				return node.isWar();
			}
		});
	}
	
	public OldPom getWarPom() {
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
		for (OldPom node: poms) {
			PomXmlParser parser = ((PomNode)node).getParser();
			String tmp = parser.dbConnectorJarRelativePath(driverClassName);
			if (tmp != null) {
				return project.concatMavenResitoryPath(tmp);
			}
		}
		return null;
	}
	
	private List<OldFolder> sourceCodeFolders = null;

	private List<OldFolder> lookupSourceCodeFolders() {
		if (sourceCodeFolders != null) {
			return sourceCodeFolders;
		}
		sourceCodeFolders =  new LinkedList<OldFolder>();
		for (OldPom pom : getPoms()) {
			OldFolder tmp = pom.getSourceCodeFolder();
			if (tmp != null) {
				sourceCodeFolders.add(tmp);
			}
		}
		return sourceCodeFolders;
	}

	private interface FolderSelector {
		public boolean isSelected(OldFolder folder);
	}
	
	private List<OldFolder> iterateSourceCodeFolders(FolderSelector selector) {
		List<OldFolder> sourceCodeFolders = lookupSourceCodeFolders();
		List<OldFolder> folders = new LinkedList<OldFolder>();
		for (OldFolder sourceCodeFolder: sourceCodeFolders) {
			for (OldFolder folder: sourceCodeFolder.getAllFolders()) {
				if (selector.isSelected(folder)) {
					folders.add(folder);
				}
			}
		}
		return folders;
	}
	
	public List<OldFolder> lookupDaoFolders() {
		return iterateSourceCodeFolders(new FolderSelector() {
			@Override
			public boolean isSelected(OldFolder folder) {
				String path = folder.path();
				if (path.endsWith("dao") || path.endsWith("mapper") || path.endsWith("repository")) {
					return true;
				}
				return false;
			}
		});
	}

	public List<OldFolder> lookupModelFolders() {
		return iterateSourceCodeFolders(new FolderSelector() {
			@Override
			public boolean isSelected(OldFolder folder) {
				String path = folder.path();
				if (path.endsWith("domain") || path.endsWith("entity") || path.endsWith("model")) {
					return true;
				}
				return false;
			}
		});
	}
	
	public List<OldFolder> lookupResourceFolders() {
		List<OldFolder> resourceFolders = new LinkedList<OldFolder>();
		for (OldPom pom: poms) {
			resourceFolders.addAll(pom.getResourceFolders());
		}
		return resourceFolders;
	}

}
