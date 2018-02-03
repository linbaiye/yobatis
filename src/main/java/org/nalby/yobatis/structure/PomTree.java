/*
 *    Copyright 2018 the original author or authors.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *    use this file except in compliance with the License.  You may obtain a copy
 *    of the License at
 *    
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *    License for the specific language governing permissions and limitations under
 *    the License.
 */
package org.nalby.yobatis.structure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.nalby.yobatis.exception.InvalidPomException;
import org.nalby.yobatis.exception.UnsupportedProjectException;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.PropertyUtil;
import org.nalby.yobatis.util.TextUtil;
import org.nalby.yobatis.xml.PomXmlParser;

/**
 * PomTree is responsible for parsing the pom tree. Also it will
 * find out the entry pom, source code folders, dao folders, resource folders.
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
			try {
				File file = folder.findFile("pom.xml");
				try (InputStream inputStream = file.open()) {
					pomXmlParser = new PomXmlParser(inputStream);
				}
			} catch (Exception e) {
				throw new InvalidPomException(e);
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
				return project.concatMavenRepositoryPath(tmp);
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


	private interface FolderSelector {
		public boolean isSelected(Folder folder);
	}


	private List<Folder> iterateSourceCodeFolders(FolderSelector selector) {
		List<Folder> sourceCodeFolders = lookupSourceCodeFolders();
		List<Folder> folders = new LinkedList<Folder>();
		for (Folder sourceCodeFolder: sourceCodeFolders) {
			for (Folder folder: Project.listAllFolders(sourceCodeFolder)) {
				if (selector.isSelected(folder)) {
					folders.add(folder);
				}
			}
		}
		return folders;
	}
	
	
	private boolean isDaoPath(String path)  {
		return path.endsWith("dao") || path.endsWith("mapper") || path.endsWith("repository");
	}
	
	private boolean isModelPath(String path)  {
		return path.endsWith("domain") || path.endsWith("entity") || path.endsWith("model");
	}
	
	/**
	 * Find all dao folders of this project.
	 * @return the folders, or an empty list.
	 */
	public List<Folder> lookupDaoFolders() {
		return iterateSourceCodeFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return isDaoPath(folder.path());
			}
		});
	}

	/**
	 * Find all model folders of this project.
	 * @return the folders, or an empty list.
	 */
	public List<Folder> lookupModelFolders() {
		return iterateSourceCodeFolders(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return isModelPath(folder.path());
			}
		});
	}

	/**
	 * Find all resource folders of this project.
	 * @return the folders, or an empty list.
	 */
	public List<Folder> lookupResourceFolders() {
		List<Folder> resourceFolders = new LinkedList<Folder>();
		for (Pom pom: poms) {
			resourceFolders.addAll(pom.getResourceFolders());
		}
		return resourceFolders;
	}
	

	private class FolderTokenSimilarityMatcher extends TokenSimilarityMatcher<Folder> {
		public FolderTokenSimilarityMatcher(Folder folder) {
			String packageName = FolderUtil.extractPackageName(folder.path());
			setTokens(new HashSet<>(Arrays.asList(packageName.split("\\."))));
		}
		@Override
		protected String[] tokenize(Folder t) {
			String packageName = FolderUtil.extractPackageName(t.path());
			return packageName.split("\\.");
		}
	}
	
	private Folder matchDaoFolder(List<Folder> folders, Folder modelFolder) {
		if (folders.isEmpty()) {
			return null;
		}
		FolderTokenSimilarityMatcher matcher  = new FolderTokenSimilarityMatcher(modelFolder);
		for (Folder folder : folders) {
			matcher.calculateSimilarity(folder);
		}
		return matcher.findMostMatchingOne();
	}
	

	public Folder findMostMatchingResourceFolder(Folder modelFolder) {
		for (Pom pom : poms) {
			Folder sourceCodeFolder = pom.getSourceCodeFolder();
			if (sourceCodeFolder == null) {
				continue;
			}
			for (Folder folder : Project.listAllFolders(pom.getSourceCodeFolder())) {
				if (modelFolder != folder) {
					continue;
				}
				Set<Folder> resourceFolders = pom.getResourceFolders();
				if (!resourceFolders.isEmpty()) {
					return resourceFolders.iterator().next();
				}
			}
		}
		if (webpom != null) {
			Set<Folder> resourceFolders = webpom.getResourceFolders();
			if (!resourceFolders.isEmpty()) {
				return resourceFolders.iterator().next();
			}
		}
		return null;
	}
	
	
	public Folder findMostMatchingDaoFolder(Folder modelFolder) {
		Expect.notNull(modelFolder, "modelPackageName must not be null.");
		List<Folder> daoFolders = new ArrayList<>();
		for (Pom pom : poms) {
			Folder sourceCodeFolder = pom.getSourceCodeFolder();
			if (sourceCodeFolder == null) {
				continue;
			}
			boolean found = false;
			// There might be more than one dao folders under this pom.
			daoFolders.clear();
			for (Folder folder : Project.listAllFolders(sourceCodeFolder)) {
				if (isDaoPath(folder.path())) {
					daoFolders.add(folder);
				} else if (modelFolder == folder) {
					found = true;
				}
			}
			if (found) {
				break;
			}
		}
		/* 
		 * Let's see if there is a dao folder has a similar package pattern to the
		 * model package pattern under the same pom.
		 */
		Folder folder = matchDaoFolder(daoFolders, modelFolder);
		if (folder == null) {
			List<Folder> folders = lookupDaoFolders();
			/* Forget about the same pom, search under the whole project. */
			folder = matchDaoFolder(folders, modelFolder);
			if (folder == null && !folders.isEmpty()) {
				/* Nothing matched, just return one. */
				folder = folders.get(0);
			}
		}
		return folder;
	}
}
