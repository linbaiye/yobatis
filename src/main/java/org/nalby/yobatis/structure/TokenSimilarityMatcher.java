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
