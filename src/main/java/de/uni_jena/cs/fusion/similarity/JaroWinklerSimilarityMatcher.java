package de.uni_jena.cs.fusion.similarity;

/*-
 * #%L
 * LakeBase Semantic Service
 * %%
 * Copyright (C) 2018 Heinz Nixdorf Chair for Distributed Information Systems, Friedrich Schiller University Jena
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface JaroWinklerSimilarityMatcher<T> {

	public final static int COMMON_PREFIX_LENGTH_LIMIT = 4;
	public final static double BOOST_THRESHOLD = 0.7;
	public final static double BOOST_FACTOR = 0.1;

	/**
	 * 
	 * @param threshold
	 *            minimum similarity value to pass matching
	 * @param query
	 *            {@link String} to match
	 * @return {@link Map} of passed entities and the corresponding similarity
	 */
	public Map<T, Double> match(double threshold, String query);

	/**
	 * 
	 * @param threshold
	 *            minimum similarity value to pass matching
	 * @param queries
	 *            {@link Collection} of {@link String}s to match
	 * @return {@link Map} containing the matched {@link String}s and a
	 *         {@link Map} of passed entities and the corresponding similarity
	 */
	default Map<String, Map<T, Double>> matchAll(double threshold, Collection<String> queries) {
		Map<String, Map<T, Double>> results = new HashMap<String, Map<T, Double>>();
		for (String string : queries) {
			results.put(string, match(threshold, string));
		}
		return results;
	}

	default Map<String, Map<T, Double>> mergeResults(Map<String, Map<T, Double>> left,
			Map<String, Map<T, Double>> right) {
		if (left == null || left.isEmpty()) {
			return right;
		} else if (right == null || right.isEmpty()) {
			return left;
		} else {
			for (String key : right.keySet()) {
				if (left.containsKey(key)) {
					left.get(key).putAll(right.get(key));
				} else {
					left.put(key, right.get(key));
				}
			}
			return left;
		}
	}
}
