package org.nalby.yobatis.mybatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nalby.yobatis.sql.Table;
import org.nalby.yobatis.structure.Folder;
import org.nalby.yobatis.util.Expect;
import org.nalby.yobatis.util.FolderUtil;
import org.nalby.yobatis.util.TextUtil;

public class TokenSimilarityGrouper implements ContextGrouper {
	
	private Map<String, TokenCounter> tokenAnalyzer;
	
	public TokenSimilarityGrouper(List<Folder> modelFolders) {
		Expect.notNull(modelFolders, "modelfolders must not be empty.");
		tokenAnalyzer = new HashMap<>();
		for (Folder folder : modelFolders) {
			String packageName = FolderUtil.extractPackageName(folder.path());
			if (!TextUtil.isEmpty(packageName)) {
				tokenAnalyzer.put(packageName, new TokenCounter(packageName));
			}
		}
		if (tokenAnalyzer.isEmpty()) {
			throw new IllegalArgumentException("no package name found.");
		}
	}
	
	private class TokenCounter {
		private Set<String> packageTokens;

		private Map<Table, Integer> tableScores;

		public TokenCounter(String packageName) {
			packageTokens = new HashSet<>();
			for (String tmp: packageName.split("\\.")) {
				packageTokens.add(tmp.toLowerCase());
			}
			tableScores = new HashMap<>();
		}
		public void calculateTableScore(Table table) {
			String[] tokens = table.getName().split("_");
			int score = 0;
			for (String token: tokens) {
				if (packageTokens.contains(token.toLowerCase())) {
					score++;
				}
			}
			tableScores.put(table, score);
		}

		public int getScore(Table table) {
			return tableScores.get(table);
		}
	}

	
	@Override
	public List<TableGroup> group(List<Table> tableList) {
		Expect.notNull(tableList, "tableList must not be null.");
		for (Table table : tableList) {
			for (TokenCounter tokenCounter : tokenAnalyzer.values()) {
				tokenCounter.calculateTableScore(table);
			}
		}
		Map<String, TableGroup> packageToGroup = new HashMap<>();
		for (Table table : tableList) {
			int maxScore = -1;
			String targetPackage = null;
			for (String packageName: tokenAnalyzer.keySet()) {
				TokenCounter tokenCounter = tokenAnalyzer.get(packageName);
				int tmp = tokenCounter.getScore(table);
				if (maxScore == -1 || maxScore < tmp) {
					targetPackage = packageName;
					maxScore = tmp;
				}
			}
			if (!packageToGroup.containsKey(targetPackage)) {
				packageToGroup.put(targetPackage, new TableGroup(targetPackage));
			}
			TableGroup tableGroup = packageToGroup.get(targetPackage);
			tableGroup.addTable(table);
		}
		return new ArrayList<>(packageToGroup.values());
	}
}
