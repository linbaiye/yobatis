package org.nalby.yobatis.mybatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.structure.TokenSimilarityMatcher;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TextUtil;

/**
 * Given a list of model folders and another list of tables, TokenSimilarityGrouper 
 * finds out a table fits which model folder most.
 * 
 * @author Kyle Lin
 *
 */
public class TokenSimilarityTableGrouper implements ContextGrouper {
	
	private Map<Folder, TableTokenSimilarityMatcher> tokenAnalyzer;
	
	public TokenSimilarityTableGrouper(List<Folder> modelFolders) {
		Expect.notNull(modelFolders, "modelfolders must not be empty.");
		tokenAnalyzer = new HashMap<>();
		for (Folder folder : modelFolders) {
			String packageName = FolderUtil.extractPackageName(folder.path());
			if (!TextUtil.isEmpty(packageName)) {
				tokenAnalyzer.put(folder, new TableTokenSimilarityMatcher(packageName));
			}
		}
		if (tokenAnalyzer.isEmpty()) {
			throw new IllegalArgumentException("no package name found.");
		}
	}
	
	private class TableTokenSimilarityMatcher extends TokenSimilarityMatcher<Table> {
		public TableTokenSimilarityMatcher(String packageName) {
			String[] tokens = packageName.split("\\.");
			setTokens(new HashSet<>(Arrays.asList(tokens)));
		}

		@Override
		protected String[] tokenize(Table t) {
			return t.getName().split("_");
		}
	}

	
	@Override
	public List<TableGroup> group(List<Table> tableList) {
		Expect.notNull(tableList, "tableList must not be null.");
		for (Table table : tableList) {
			for (TableTokenSimilarityMatcher tokenCounter : tokenAnalyzer.values()) {
				tokenCounter.calculateSimilarity(table);
			}
		}
		Map<Folder, TableGroup> folderToGroup = new HashMap<>();
		for (Table table : tableList) {
			int maxScore = -1;
			Folder targetFolder = null;
			for (Folder folder: tokenAnalyzer.keySet()) {
				/* Find out which matcher has the highest score of this table. */
				TableTokenSimilarityMatcher matcher = tokenAnalyzer.get(folder);
				int thisScore = matcher.getScore(table);
				if (maxScore < thisScore) {
					maxScore = thisScore;
					targetFolder = folder;
				}
			}
			if (!folderToGroup.containsKey(targetFolder)) {
				folderToGroup.put(targetFolder, new TableGroup(targetFolder));
			}
			TableGroup tableGroup = folderToGroup.get(targetFolder);
			tableGroup.addTable(table);
		}
		return new ArrayList<>(folderToGroup.values());
	}
}
