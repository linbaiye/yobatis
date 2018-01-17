package org.nalby.yobatis.structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nalby.yobatis.util.Expect;

public abstract class TokenSimilarityMatcher<T> {
	
	private Set<String> tokensToMatch;
	
	private Map<T, Integer> scores;
	
	protected void setTokens(Set<String> tokens) {
		Expect.asTrue(tokens != null && !tokens.isEmpty(), "tokens must not be empty.");
		this.tokensToMatch = new HashSet<>();
		for (String token: tokens) {
			this.tokensToMatch.add(token.toLowerCase());
		}
		this.scores = new HashMap<>();
	}

	protected abstract String[] tokenize(T t);
	
	public void calculateSimilarity(T target) {
		Expect.notNull(target, "target must not be null.");
		String[] tokens = tokenize(target);
		int score = 0;
		for (String token: tokens) {
			if (tokensToMatch.contains(token.toLowerCase())) {
				score++;
			}
		}
		scores.put(target, score);
	}
	
	public T findMostMatchingOne() {
		T result = null;
		int maxScore = -1;
		for (T tmp : scores.keySet()) {
			int thisScore = scores.get(tmp);
			if (maxScore < thisScore) {
				maxScore = thisScore;
				result = tmp;
			}
		}
		return result;
	}
	
	public Integer getScore(T t) {
		return scores.containsKey(t) ? scores.get(t) : -1;
	}

}
