package org.nalby.yobatis.structure;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.nalby.yobatis.util.Expect;

public class RootFolder {

	private Folder root;
	
	private List<Folder> leafFolders = null;

	private void parsePackages() {
		if (leafFolders != null) {
			return;
		}
		leafFolders = new LinkedList<Folder>();
		Stack<Folder> stack = new Stack<Folder>();
		stack.push(root);
		do {
			Folder node = stack.pop();
			List<Folder> subFolders = node.folders();
			for (Folder child: subFolders) {
				if (child.containsFolders()) {
					stack.push(child);
				} else {
					//A leaf node.
					leafFolders.add(child);
				}
			}
		} while (!stack.isEmpty());
	}
	
	
	private interface FolderSelector {
		public boolean isSelected(Folder folder);
	}

	private String selectFolderPath(FolderSelector selector) {
		parsePackages();
		for (Folder folder: leafFolders) {
			if (selector.isSelected(folder)) {
				return folder.path();
			}
		}
		return null;
	}

	public String modelFolderPath() {
		return selectFolderPath(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return folder.isModelLayer();
			}
		});
	}
	
	public String daoFolderPath() {
		return selectFolderPath(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return folder.isDaoLayer();
			}
		});
	}

	public RootFolder(Folder root) {
		Expect.notNull(root, "root folder must not be empty.");
		this.root = root;
	}

}
