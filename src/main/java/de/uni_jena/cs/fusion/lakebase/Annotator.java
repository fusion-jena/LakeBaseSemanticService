package de.uni_jena.cs.fusion.lakebase;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.IRI;

import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSource;
import de.uni_jena.cs.fusion.semantic.datasource.SemanticDataSourceException;
import de.uni_jena.cs.fusion.util.stopwords.StopWords;

/**
 * 
 * @since 0.1
 *
 */
public class Annotator {

	private SemanticDataSource adapterManager;

	/**
	 * punctuation characters not contained in annotated terms
	 * 
	 * NOTE: "," and "." must not have a digit subsequently, to exclude decimal
	 * marks.
	 */
	private final static String[] punctuations = { ",(?!\\d)", "\\.(?!\\d)", ";", "\\?", "!", "\"", "\\(", "\\)", "\\[",
			"\\]", "\\{", "\\}" };
	/**
	 * regular expression to split queries into parts to annotate
	 */
	public static final String punctuationRegEx = " *" + StringUtils.join(punctuations, " *| *") + " *";
	public static final int maxWordsToProcess = 10;

	public Annotator(SemanticDataSource adapterManager) {
		this.adapterManager = adapterManager;
	}

	/**
	 * Determines annotations for a given {@link String} in a given scope.
	 * 
	 * @param query
	 *            the {@link String} to annotate
	 * @param scopes
	 *            the scopes of the annotations
	 * @return a {@link Map} of annotated terms and annotation {@link IRI}s
	 * @throws AnnotatorException
	 */
	public Map<String, IRI> determineAnnotations(String query, Collection<Scope> scopes) throws AnnotatorException {
		return this.determineAnnotations(query, Collections.emptyMap(), Collections.emptyMap(), scopes);
	}

	/**
	 * Determines annotations for a given {@link String} in a given scope,
	 * considering predefined annotations and excluded terms.
	 * 
	 * @param query
	 *            the {@link String} to annotate
	 * @param predefined
	 *            the predefined annotations to consider
	 * @param excluded
	 *            the {@link Map} of terms and {@link IRI}s to exclude from
	 *            annotation
	 * @param scopes
	 *            the scope of the annotations
	 * @return a {@link Map} of annotated terms and annotation {@link IRI}s
	 * @throws AnnotatorException
	 */
	public Map<String, IRI> determineAnnotations(String query, Map<String, IRI> predefined,
			Map<String, Collection<IRI>> excluded, Collection<Scope> scopes) throws AnnotatorException {
		Map<String, IRI> determined = new HashMap<String, IRI>();

		// clean up query
		query = clean(query);

		// check predefined annotations
		for (String term : predefined.keySet()) {
			// use predefined annotation
			determined.put(term, predefined.get(term));

			// replace predefined term to avoid duplicated annotation
			query.replaceAll("(^| )+" + term + "( |$)+", punctuations[0]);
		}

		// get proposed annotations
		Map<String, Map<IRI, Double>> proposed = getAnnotations(query, true, false, predefined, excluded, scopes);

		for (String term : proposed.keySet()) {
			// get candidates map
			Map<IRI, Double> candidates = proposed.get(term);

			// get best matching term
			// http://stackoverflow.com/a/37374726/3637482
			IRI bestMatch = Collections.max(candidates.entrySet(), Map.Entry.comparingByValue()).getKey();

			// use best matching annotation
			determined.put(term, bestMatch);
		}

		return determined;
	}

	/**
	 * Proposes annotations for a given string.
	 * 
	 * @param query
	 *            the {@link String} to annotate
	 * @param scopes
	 *            the scope of the annotations
	 * @return a map with one entry per query containing a list of matching concepts
	 *         descending sorted by rank
	 * @throws SemanticDataSourceException
	 */
	public Map<String, Map<IRI, Double>> proposeAnnotations(String query, Collection<Scope> scopes)
			throws AnnotatorException {
		return getAnnotations(query, false, false, Collections.emptyMap(), Collections.emptyMap(), scopes);
	}

	/**
	 * Proposes annotations for a given string.
	 * 
	 * @param query
	 *            the {@link String} to annotate
	 * @param predefined
	 *            the predefined annotations to consider
	 * @param excluded
	 *            the {@link Map} of terms and {@link IRI}s to exclude from
	 *            annotation
	 * @param scopes
	 *            the scope of the annotations
	 * @return a map with one entry per query containing a list of matching concepts
	 *         descending sorted by rank
	 * @throws SemanticDataSourceException
	 */
	public Map<String, Map<IRI, Double>> proposeAnnotations(String query, Map<String, IRI> predefined,
			Map<String, Collection<IRI>> excluded, Collection<Scope> scopes) throws AnnotatorException {
		return getAnnotations(query, false, false, predefined, excluded, scopes);
	}

	/**
	 * Augments annotations for a given string by adding annotations for not
	 * annotated terms. The Results will not contain further annotations for already
	 * annotated terms.
	 * 
	 * @param query
	 *            the {@link String} to annotate
	 * @param predefined
	 *            the predefined annotations to consider
	 * @param excluded
	 *            the {@link Map} of terms and {@link IRI}s to exclude from
	 *            annotation
	 * @param scopes
	 *            the scope of the annotations
	 * @return a map with one entry per query containing a list of matching concepts
	 *         descending sorted by rank
	 * @throws SemanticDataSourceException
	 */
	public Map<String, Map<IRI, Double>> augmentAnnotations(String query, Map<String, IRI> predefined,
			Map<String, Collection<IRI>> excluded, Collection<Scope> scopes) throws AnnotatorException {
		return getAnnotations(query, false, true, predefined, excluded, scopes);
	}

	/**
	 * Augments annotations for a given string by adding annotations for not
	 * annotated terms. The Results will not contain further annotations for already
	 * annotated terms.
	 * 
	 * @param query
	 *            the {@link String} to annotate
	 * @param predefined
	 *            the predefined annotations to consider
	 * @param excluded
	 *            the {@link Map} of terms and {@link IRI}s to exclude from
	 *            annotation
	 * @return a map with one entry per query containing a list of matching concepts
	 *         descending sorted by rank
	 * @throws SemanticDataSourceException
	 */
	public Map<String, Map<IRI, Double>> augmentAnnotations(String query, Map<String, IRI> predefined,
			Map<String, Collection<IRI>> excluded) throws AnnotatorException {
		return getAnnotations(query, false, true, predefined, excluded, Collections.singleton(Scope.allIncludingFallback));
	}

	/**
	 * Get annotations for a given string.
	 * 
	 * TODO docu
	 * 
	 * @param query
	 *            the {@link String} to annotate
	 * @param distinct
	 *            if <code>true</code> the results will not contain results for
	 *            subterms of terms, that provide a result
	 * @param skipping
	 *            if <code>true</code> the result will not contain further results
	 *            for terms contained in the set of predefined terms
	 * @param predefined
	 *            the predefined annotations to consider
	 * @param excluded
	 *            the {@link Map} of terms and {@link IRI}s to exclude from
	 *            annotation
	 * @param scopes
	 *            the scope of the annotations
	 * @return a map with one entry per query containing a list of matching concepts
	 *         descending sorted by rank
	 * @throws SemanticDataSourceException
	 */
	Map<String, Map<IRI, Double>> getAnnotations(String query, boolean distinct, boolean skipping,
			Map<String, IRI> predefined, Map<String, Collection<IRI>> excluded, Collection<Scope> scopes)
			throws AnnotatorException {

		// initialize results
		Map<String, Map<IRI, Double>> results = new HashMap<String, Map<IRI, Double>>();

		// clean up query
		query = clean(query);

		// split at punctuation and iterate parts
		for (String queryFragment : query.split(punctuationRegEx)) {

			if (0 < queryFragment.length()) {

				// initialize list of terms to match
				List<String> terms = new ArrayList<String>();

				// generate word list
				String[] words = queryFragment.split(" ");

				// iterate first word of term
				for (int i = 0; i < words.length; i++) {

					// iterate last word of term (backward)
					for (int j = Math.min(i + maxWordsToProcess, words.length); j > i; j--) {

						// add word sequence to term list, filtering stop words
						String term = StringUtils.join(words, " ", i, j);
						if (!StopWords.isStopWord(term)) {
							terms.add(term);
						}
					}
				}

				// match term list
				Map<String, Map<IRI, Double>> match;
				try {
					match = this.adapterManager.getMatches(terms, Scope.getIris(scopes));
				} catch (SemanticDataSourceException e) {
					throw new AnnotatorException("Failed to match the given term.", e);
				}

				// initialize skip position (end of term must be after the skip
				// position, to make terms distinct)
				int skipPosition = 0;

				// select results
				// iterate first word of term again
				for (int i = 0; i < words.length; i++) {

					// iterate last word of term (backward) again
					for ( // s tart with max number of words
							int j = Math.min(i + maxWordsToProcess, words.length);
							// stop, if last word is before first word or before
							// the skip position
							j > Math.max(i, skipPosition);
							// remove last word
							j--) {

						String term = StringUtils.join(words, " ", i, j);

						if (predefined.containsKey(term)) {
							// annotation for the term is predefined

							Double predefinedRank = match.getOrDefault(term, Collections.emptyMap())
									.getOrDefault(predefined.get(term), 1.0);

							if (skipping) {
								// use predefined annotation
								results.put(term, Collections.singletonMap(predefined.get(term), predefinedRank));
							} else {
								results.put(term, match.getOrDefault(term, new HashMap<IRI, Double>()));
								// enforce containing predefined
								results.get(term).put(predefined.get(term), predefinedRank);
							}

							// do not select subterms
							skipPosition = j;
							break;
						} else {
							if (match.containsKey(term)) {
								// term provided results

								// remove excluded IRIs for the term
								if (excluded.containsKey(term)) {
									for (IRI excludedIri : excluded.get(term)) {
										match.get(term).remove(excludedIri);
									}
								}

								if (!match.get(term).isEmpty()) {
									// term still provided results

									// put term and match list to results
									results.put(term, match.get(term));

									if (distinct) {
										// distinct results are requested

										// do not select subterms
										skipPosition = j;
										break;
									}
								}
							}
						}
					}
				}
			}
		}

		return results;
	}

	/**
	 * Returns a cleaned version of the given {@link String}.
	 * 
	 * @param str
	 *            the {@link String} to clean
	 * @return cleaned version of the given {@link String}
	 */
	private static String clean(String str) {
		// replace all whitespace sequences by a single space
		return str.replaceAll("\\s+", " ");
	}
}
