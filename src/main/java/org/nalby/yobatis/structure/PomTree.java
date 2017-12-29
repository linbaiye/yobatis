package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.nalby.yobatis.exception.InvalidConfigurationException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
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

	private Map<PomXmlParser, Folder> pomXmlParsers;

	private Pom root;

	private Pom webpom;
	
	private Set<Pom> poms;
	
	public PomTree(Project project) {
		try {
			this.project = project;
			this.root = new PomNode(null, this.project);
			selectAllPoms();
		} catch (Exception e) {
			throw new UnsupportedProjectException(e);
		}
	}

	private class PomNode implements Pom {
		private PomXmlParser pomXmlParser;

		private List<Pom> children;

		private Pom parent;

		private Folder folder;
		
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
			constructSubNodes();
			loadResourceAndWebappFolders();
		}

		private void constructSubNodes() {
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
		public Pom parent() {
			return parent;
		}

		@Override
		public List<Pom> children() {
			return children;
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
		public String name() {
			return folder.name();
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
	}
	
	public Set<Pom> getPoms() {
		return poms;
	}
	
	public void dump() {
		Stack<Pom> stack = new Stack<Pom>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Pom node = stack.pop();
			for (Pom item : node.children()) {
				stack.push(item);
			}
		}
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
			for (Pom item : node.children()) {
				stack.push(item);
			}
		}
		return null;
	}
	
	private void selectAllPoms() {
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
		for (PomXmlParser parser : pomXmlParsers.keySet()) {
			String tmp = parser.dbConnectorJarRelativePath(driverClassName);
			if (tmp != null) {
				return project.concatMavenResitoryPath(tmp);
			}
		}
		return null;
	}
}
