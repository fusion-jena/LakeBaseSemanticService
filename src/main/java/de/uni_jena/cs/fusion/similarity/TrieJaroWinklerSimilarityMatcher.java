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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.uni_jena.cs.fusion.collection.Trie;
import de.uni_jena.cs.fusion.collection.Tries;

/**
 * 
 * @author Jan Martin Keil
 *
 *         proceeded calculation of the similarity value per node (using many
 *         parameters)
 *
 */
public class TrieJaroWinklerSimilarityMatcher<T> implements JaroWinklerSimilarityMatcher<T> {

	private Trie<T> trie;

	public TrieJaroWinklerSimilarityMatcher(Trie<T> trie) {
		this.trie = trie;
	}

	private int equalInRange(boolean[] array, boolean expected, int lowerBound, int upperBound) {
		int result = 0;
		for (int i = lowerBound; i <= upperBound; i++) {
			if (array[i] == expected) {
				result++;
			}
		}
		return result;
	}

	/**
	 * @param commonCharacters
	 *            characters in common in pair of strings
	 * @param length1
	 *            length of first string
	 * @param length2
	 *            length of second string
	 * @param halfTranspositions
	 *            number of half transpositions
	 * @return
	 */
	private static double jaroSimilarity(int commonCharacters, int length1, int length2, int halfTranspositions) {
		if (commonCharacters > 0) {
			return ((double) commonCharacters * commonCharacters * 2 / length1 + (double) commonCharacters * commonCharacters * 2 / length2 + commonCharacters * 2
					- (double) halfTranspositions) / (3 * commonCharacters * 2);
		} else {
			return 0;
		}
	}

	/**
	 * @param commonCharacters
	 * @param length1
	 * @param length2
	 * @param halfTranspositions
	 * @param commonPrefixLength
	 * @return
	 */
	private static double jaroWinklerSimilarity(int commonCharacters, int length1, int length2, int halfTranspositions,
			int commonPrefixLength) {

		double jaroSimilarity = jaroSimilarity(commonCharacters, length1, length2, halfTranspositions);

		if (jaroSimilarity >= BOOST_THRESHOLD) {
			return jaroSimilarity + commonPrefixLength * BOOST_FACTOR * (1 - jaroSimilarity);
		} else {
			return jaroSimilarity;
		}
	}

	public Map<T, Double> match(double threshold, String queryString) {
		// initialize result
		Map<T, Double> results = new HashMap<T, Double>();

		int queryLength = queryString.length(); // length of first string

		// iterate possible lengths of string2
		for (Integer termTargetLength : this.trie.containedLengths()) {
			// calculate window size for common characters
			int windowSize = windowSize(queryLength, termTargetLength);
			// max value of l = the size of the emphasized first few characters
			int maxCommonPrefixSize = Math.min(COMMON_PREFIX_LENGTH_LIMIT, Math.min(queryLength, termTargetLength));
			// recursive traverse of the trie to get matching strings of length2
			this.match(threshold, this.trie, queryString, queryLength, termTargetLength, windowSize, 0 // minCommonCharacters
					, 0 // minHalfTranspositions
					, maxCommonPrefixSize, 0 // saveCommonCharsQuery
					, new boolean[queryLength] // assignedQuery
					, new boolean[termTargetLength] // assignedTerm
					, "" // termCommonChars
					, results);
		}
		return results;
	}

	/**
	 * @param threshold
	 *            the threshold
	 * @param termTrie
	 * @param queryString
	 *            first string
	 * @param termString
	 *            second string
	 * @param queryLength
	 *            length of first string
	 * @param termTargetLength
	 *            length of second string
	 * @param windowSize
	 *            window size to search for common characters
	 * @param minCommonCharacters
	 *            min number of characters in common in pair of strings (@param
	 *            minHalfTranspositions min number of half transpositions)
	 * @param minHalfTranspositions
	 * @param maxCommonPrefixSize
	 *            max number of characters in common in pair of strings in
	 *            emphasized beginning
	 * @param saveCommonCharsQuery
	 * 
	 * @param assignedQuery
	 *            array of booleans stating which characters of first string
	 *            have been assigned (TRUE = assigned)
	 * @param assignedTerm
	 *            array of booleans stating which characters of second string
	 *            have been assigned (TRUE = assigned)
	 * @param commonCharsTerm
	 * 
	 * @return MultiMap of ranked matching strings
	 */
	private void match(double threshold, Trie<T> termTrie, String queryString, int queryLength, int termTargetLength,
			int windowSize, int minCommonCharacters, int minHalfTranspositions, int maxCommonPrefixSize,
			int saveCommonCharsQuery, boolean[] assignedQuery, boolean[] assignedTerm, String commonCharsTerm,
			Map<T, Double> results) {

		if (termTrie.containsLength(termTargetLength)) {
			// current branch contains string of target length

			// get current position on term string
			final int termCurrentNodeDepth = termTrie.depth();
			final int termCurrentNodeLength = termTrie.keyLength();

			// iterate new characters
			for (int termCurrentLength = termCurrentNodeDepth
					+ 1; termCurrentLength <= termCurrentNodeLength; termCurrentLength++) {
				// get character at current position
				final char currentTermChar = termTrie.symbol().charAt(termCurrentLength - 1 - termCurrentNodeDepth);

				// get window on query string
				final int windowLowerBound = Math.max(termCurrentLength - 1 - windowSize, 0);
				final int windowsUpperBound = Math.min(termCurrentLength + windowSize, queryLength) - 1;

				// update maxCommonPrefixSize
				if (termCurrentLength <= maxCommonPrefixSize
						&& queryString.charAt(termCurrentLength - 1) != currentTermChar)
				// currently in the prefix and characters at current position
				// do not match
				{
					// reduce maxCommonPrefixSize to current depth
					maxCommonPrefixSize = termCurrentLength - 1;
				}

				// search matching char for current term char in window
				for (int i = windowLowerBound; i <= windowsUpperBound; i++) {
					if (!assignedQuery[i] && queryString.charAt(i) == currentTermChar) {
						// unassigned common character was found

						assignedQuery[i] = true;
						assignedTerm[termCurrentLength - 1] = true;
						minCommonCharacters++;
						commonCharsTerm = commonCharsTerm + currentTermChar;
						break;
					}
				}

				// update minHalfTranspositions
				if (windowSize < termCurrentLength && termCurrentLength - windowSize <= queryLength) {
					// window lower bound inside of query string
					if (assignedQuery[windowLowerBound]) {
						// character at window lower bound is assigned
						saveCommonCharsQuery++;
						if (queryString.charAt(windowLowerBound) != commonCharsTerm.charAt(saveCommonCharsQuery - 1)) {
							// common characters at last save position not equal
							minHalfTranspositions++;
						}
					}
				}
			}

			// get window bounds
			int windowLowerBound = Math.max(termCurrentNodeLength - 1 - windowSize, 0);
			int windowsUpperBound = Math.min(termTargetLength + windowSize, queryLength) - 1;
			// get number of characters that can still become assigned
			int assignableQuery = equalInRange(assignedQuery, false, windowLowerBound, windowsUpperBound);
			int assignableTerm = termTargetLength - termCurrentNodeLength;
			// get maximum number of common characters
			int maxCommonCharacters = Math.min(assignableQuery, assignableTerm) + minCommonCharacters;

			// get remaining half transpositions
			if (termCurrentNodeLength == termTargetLength) {
				// termString has been completed

				// iterate assignments not covered by minHalfTransposition yet
				for (int i = Math.max(termCurrentNodeLength - windowSize, 0); i <= windowsUpperBound; i++) {

					if (assignedQuery[i]) {
						// position is assigned

						saveCommonCharsQuery++;
						if (queryString.charAt(i) != commonCharsTerm.charAt(saveCommonCharsQuery - 1)) {
							// common characters at current position not equal

							minHalfTranspositions++;
						}
					}
				}
			}

			// calculate max similarity
			double maxSimilarity = jaroWinklerSimilarity(maxCommonCharacters, queryLength, termTargetLength,
					minHalfTranspositions, maxCommonPrefixSize);

			// check against threshold
			if (maxSimilarity >= threshold) {
				// threshold is meet
				if (termTargetLength == termCurrentNodeLength) {
					// current node has target depth

					if (termTrie.isPopulated()) {
						// current node is contained
						// add object of current node to results
						results.put(termTrie.value(), maxSimilarity);
					}
				} else {
					// iterate children
					Iterator<? extends Trie<T>> children = termTrie.childrenIterator();

					while (children.hasNext()) {

						boolean[] termAssignedCopy = Arrays.copyOf(assignedTerm, termTargetLength);
						boolean[] queryAssignedCopy = Arrays.copyOf(assignedQuery, queryLength);

						Trie<T> child = children.next();

						// traverse child
						match(threshold, child, queryString, queryLength, termTargetLength, windowSize,
								minCommonCharacters, minHalfTranspositions, maxCommonPrefixSize, saveCommonCharsQuery,
								queryAssignedCopy, termAssignedCopy, commonCharsTerm, results);
					}
				}
			}
		}
	}

	private static int windowSize(int length1, int length2) {
		return Math.max(0, Math.max(length1, length2) / 2 - 1);
	}

	/**
	 * Returns the Jaro-Winkler Similarity of two given {@link String}s.
	 * 
	 * @param first
	 *            first {@link String} to match
	 * @param second
	 *            second {@link String} to match
	 * @return similarity of {@code first} and {@code second}
	 */
	public static double match(String first, String second) {
		return new TrieJaroWinklerSimilarityMatcher<String>(Tries.singletonTrieSet(first)).match(0, second).get(first);
	}

}
