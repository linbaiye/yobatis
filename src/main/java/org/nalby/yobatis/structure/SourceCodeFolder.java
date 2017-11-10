package org.nalby.yobatis.structure;
import java.util.List;
import java.util.Stack;
import org.nalby.yobatis.util.Expect;

public class SourceCodeFolder {

	private Folder root;

	private Folder parsePackages(FolderSelector selector) {
		Stack<Folder> stack = new Stack<Folder>();
		stack.push(root);
		do {
			Folder node = stack.pop();
			List<Folder> subFolders = node.getSubFolders();
			for (Folder child: subFolders) {
				if (selector.isSelected(child)) {
					return child;
				}
				if (child.containsFolders()) {
					stack.push(child);
				}
			}
		} while (!stack.isEmpty());
		return null;
	}
	
	private interface FolderSelector {
		public boolean isSelected(Folder folder);
	}

	public String modelFolderPath() {
		Folder folder = parsePackages(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return true;
				//return folder.isModelLayer();
			}
		});
		return folder == null? null : folder.path();
	}
	
	public String daoFolderPath() {
		Folder folder = parsePackages(new FolderSelector() {
			@Override
			public boolean isSelected(Folder folder) {
				return true;
				//return folder.isDaoLayer();
			}
		});
		return folder == null? null : folder.path();
	}

	public SourceCodeFolder(Folder root) {
		Expect.notNull(root, "root folder must not be empty.");
		this.root = root;
	}
	
	public String getPath() {
		return root.path();
	}

}
